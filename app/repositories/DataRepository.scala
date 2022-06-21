package repositories

import akka.actor.Status.Success
import com.mongodb.client.result.InsertOneResult
import models.APIError.BadAPIResponse
import models.{APIError, DataModel}
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.Updates.set
import org.mongodb.scala.model.Filters.{empty, equal}
import org.mongodb.scala.model._
import org.mongodb.scala.result
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.SECONDS
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DataRepository @Inject()(mongoComponent: MongoComponent)(implicit ec: ExecutionContext) extends PlayMongoRepository[DataModel](
  collectionName = "dataModels",
  mongoComponent = mongoComponent,
  domainFormat = DataModel.formats,
  indexes = Seq(IndexModel(
    Indexes.ascending("_id"),
    IndexOptions().name("ttlIndex").unique(true).expireAfter(0, SECONDS))
  ,IndexModel(
      Indexes.ascending("name"),
      IndexOptions().name("name").unique(true).expireAfter(0, SECONDS))
  ,IndexModel(
      Indexes.ascending("description"),
      IndexOptions().name("description").unique(true).expireAfter(0, SECONDS))
  ,IndexModel(
      Indexes.ascending("numSales"),
      IndexOptions().name("numSales").unique(true).expireAfter(0, SECONDS))
  )
) {

  val emptyData = new DataModel("empty", "", "", 0)
  val errorData = new DataModel("error", "", "", 0)

  def index(): Future[Either[APIError, Seq[JsValue]]] = {
    collection.find().toFuture().map{
      case books: Seq[DataModel] => Right(books.map(book => Json.toJson(book)))
      case _ => Left(APIError.BadAPIResponse(400, "Unknown error"))
    }
  }

  def create(book: DataModel): Future[Either[APIError, DataModel]] =
    collection.insertOne(book).toFutureOption().map {
      case Some(result: InsertOneResult) if result.wasAcknowledged() => Right(book)
      case _ => Left(APIError.BadAPIResponse(400, "Bad Request"))
    }

  private def byID(id: String): Bson = {
    Filters.and(
      Filters.equal("_id", id)
    )
  }

  private def byName(name: String): Bson =
    Filters.and(
      Filters.equal("name", name)
    )

  def read(findBy: String, identifier: String): Future[DataModel] = {
    if (findBy.equals("ID"))
      collection.find(byID(identifier)).headOption() flatMap {
        case Some(data) => Future(data)
        case _ => Future(emptyData)
      }
    else if (findBy.equals("name"))
      collection.find(byName(identifier)).headOption() flatMap {
        case Some(data) => Future(data)
        case _ => Future(emptyData)
      }
    else Future(errorData)
  }

  def update(id: String, book: DataModel): Future[result.UpdateResult] =
    collection.replaceOne(
      filter = byID(id),
      replacement = book,
      options = new ReplaceOptions().upsert(false)
    ).toFuture()

  def edit(id: String, fieldName: String, edit: String): Future[Option[DataModel]] = {
    collection.findOneAndUpdate(
      equal("_id", id),
      set(fieldName, edit),
      options = FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
      ).toFutureOption()
  }

  def delete(id: String): Future[Long] =
    collection.deleteOne(filter = byID(id)).toFuture().map(_.getDeletedCount)

  def deleteAll(): Future[Unit] = collection.deleteMany(empty()).toFuture().map(_ => ()) //Hint: needed for tests

}