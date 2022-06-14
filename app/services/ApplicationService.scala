package services

import cats.data.EitherT
import models.APIError.BadAPIResponse

import javax.inject.Inject
import javax.inject.Singleton
import models.{APIError, DataModel}
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.OFormat.oFormatFromReadsAndOWrites
import play.api.libs.json._
import play.api.mvc.Results.{Accepted, BadRequest, Created, Ok}
import repositories.DataRepository
import play.api.mvc._
import services.LibraryService

import java.awt.print.Book
import java.security.Provider.Service
import javax.inject._
import scala.concurrent._

@Singleton
class ApplicationService @Inject() (val dataRepository: DataRepository,
                         implicit val ec: ExecutionContext) {

  def create(request: Request[JsValue]): Future[Either[APIError, DataModel]] =
    request.body.validate[DataModel] match {
      case JsSuccess(dataModel, _) =>
        dataRepository.create(dataModel)
      case JsError(_) => Future(Left(APIError.BadAPIResponse(400, "Unable to validate request body format")))
    }

  def read(id: String): Future[Either[APIError, Result]] =
    dataRepository.read(id).map {
      case dataModel if dataModel._id.equals("empty") => Left(BadAPIResponse(400, s"Unable to find book of ID: $id"))
      case dataModel => Right(Ok(Json.toJson(dataModel)))
      case _ => Left(BadAPIResponse(400, "Unable to complete request"))
    }

  def update(id: String, newBook: DataModel): Future[Either[APIError, Result]] =
        dataRepository.update(id, newBook).map {
          case result if result.getModifiedCount.equals(0l) =>
            Left(APIError.BadAPIResponse(400, s"Unable to update book of ID: $id"))
          case result => Right(Accepted(Json.toJson(newBook)))
        }


  def delete(id: String): Future[Either[APIError, Result]] =
    dataRepository.delete(id).map {
      case 1 => Right(Accepted)
      case _ => Left(BadAPIResponse(400, s"Unable to delete book of ID: $id"))
    }

}
