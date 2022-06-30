package services

import cats.data.EitherT
import models.APIError.BadAPIResponse

import javax.inject.Inject
import javax.inject.Singleton
import models.{APIError, DataModel, UpdateField}
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.OFormat.oFormatFromReadsAndOWrites
import play.api.libs.json._
import play.api.mvc.Results.{Accepted, BadRequest, Created, Ok}
import repositories.{DataRepository, DataRepositoryTrait}
import play.api.mvc._
import services.LibraryService

import java.awt.print.Book
import java.security.Provider.Service
import javax.inject._
import scala.concurrent._

@Singleton
class ApplicationService @Inject()(dataRepository: DataRepositoryTrait)(implicit ec: ExecutionContext) {

  def index(): Future[Either[APIError, Seq[JsValue]]] = {
    dataRepository.index()
  }

  def create(request: Request[JsValue]): Future[Either[APIError, DataModel]] =
    request.body.validate[DataModel] match {
      case JsSuccess(dataModel, _) => dataRepository.create(dataModel)
      case JsError(_) => Future(Left(APIError.BadAPIResponse(415, "Unable to validate request body format")))
    }

  def read(findBy: String, identifier: String): Future[Either[APIError, Result]] = {
    dataRepository.read(findBy, identifier).map {
      case dataModel if dataModel.id.equals("empty") => Left(BadAPIResponse(404, s"Unable to find book of $findBy: $identifier"))
      case dataModel => Right(Ok(Json.toJson(dataModel)))
      case _ => Left(BadAPIResponse(400, "Unable to complete request"))
    }
  }

  def update(id: String, newBook: DataModel): Future[Either[APIError, Result]] =
    dataRepository.update(id, newBook).map {
      case Right(result) => Right(Accepted(Json.toJson(result)))
      case Left(error) =>Left(error)
//      case result if result.getModifiedCount.equals(0L) =>
//        Left(APIError.BadAPIResponse(400, s"Unable to update book of ID: $id"))
//      case result => Right(Accepted(Json.toJson(newBook)))
    }

  def edit(id: String, update: UpdateField): Future[Either[APIError, Result]] =
    dataRepository.edit(id, update.fieldName, update.edit).map {
      case None => Left(APIError.BadAPIResponse(400, s"Unable to edit book of ID: $id"))
      case Some(updatedBook) => Right(Accepted(Json.toJson(updatedBook)))
    }

  def delete(id: String): Future[Either[APIError, Result]] =
    dataRepository.delete(id).map {
      case 1 => Right(Accepted)
      case _ => Left(BadAPIResponse(400, s"Unable to delete book of ID: $id"))
    }

}
