package controllers

import cats.data.EitherT
import models.APIError.BadAPIResponse
import models.{APIError, DataModel, UpdateField}
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.OFormat.oFormatFromReadsAndOWrites
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import repositories.DataRepository
import play.api.mvc._
import services.{ApplicationService, LibraryService}

import java.awt.print.Book
import java.security.Provider.Service
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}


@Singleton
class ApplicationController @Inject()(val controllerComponents: ControllerComponents,
//                                      val dataRepository: DataRepository,
                                      val applicationService: ApplicationService,
                                      val libraryService: LibraryService,
                                      implicit val ec: ExecutionContext
                                     ) extends BaseController {

  def index(): Action[AnyContent] = Action.async { implicit request =>
    applicationService.index().map{
      case Right(book: Seq[DataModel]) => Ok(Json.toJson(book))
      case Left(error) => Status(error.httpResponseStatus)(Json.toJson(error.reason))
    }
  }

  def create(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    applicationService.create(request).map {
      case Right(value) => Created(Json.toJson(value))
      case Left(error) => Status(error.httpResponseStatus)(Json.toJson(error.reason))
    }
  }

  def readId(id: String): Action[AnyContent] = Action.async { implicit request =>
    applicationService.read("ID", id).map {
      case Right(value) => value
      case Left(error) => Status(error.httpResponseStatus)(Json.toJson(error.reason))
    }
  }
  def readName(name: String): Action[AnyContent] = Action.async { implicit request =>
    applicationService.read("name", name).map {
      case Right(value) => value
      case Left(error) => Status(error.httpResponseStatus)(Json.toJson(error.reason))
    }
  }

  def update(id: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[DataModel] match {
      case JsSuccess(data, _) =>
        applicationService.update(id, data).map {
          case Left(error) => Status(error.httpResponseStatus)(Json.toJson(error.reason))
          case Right(value) => value
        }
      case JsError(_) => Future(BadRequest)
    }
  }
  def edit(id: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[UpdateField] match {
      case JsSuccess(data, _) =>
        applicationService.edit(id, data).map {
          case Left(error) => Status(error.httpResponseStatus)(Json.toJson(error.reason))
          case Right(value) => value
        }
      case JsError(_) => Future(BadRequest)
    }
  }

  def delete(id: String): Action[AnyContent] = Action.async { implicit request =>
    applicationService.delete(id).map {
      case Right(right) => right
      case Left(error) => Status(error.httpResponseStatus)(Json.toJson(error.reason))
    }
  }

  def getGoogleBook(search: String, term: String): Action[AnyContent] = Action.async { implicit request =>
    libraryService.getGoogleBook(search = search, term = term).value.map {
      case Right(value) => Ok(Json.toJson(value))
      case Left(error) => Status(error.httpResponseStatus)(Json.toJson(error.reason))
    }
  }
}
