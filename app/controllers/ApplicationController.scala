package controllers

import models.DataModel
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.OFormat.oFormatFromReadsAndOWrites
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import repositories.DataRepository
import play.api.mvc._
import services.LibraryService

import java.awt.print.Book
import java.security.Provider.Service
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}


@Singleton
class ApplicationController @Inject()(val controllerComponents: ControllerComponents,
                                      val dataRepository: DataRepository,
                                      val service: LibraryService,
                                      implicit val ec: ExecutionContext
                                     ) extends BaseController {

  def index(): Action[AnyContent] = Action.async { implicit request =>
    val books: Future[Seq[DataModel]] = dataRepository.collection.find().toFuture()
    books.map(items => Json.toJson(items)).map(result => Ok(result))
  }

  def create(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[DataModel] match {
      case JsSuccess(dataModel, _) =>
        dataRepository.create(dataModel).map(result => Created(Json.toJson(result)))
      case JsError(_) => Future(BadRequest)
    }
  }

  def read(id: String): Action[AnyContent] = Action.async { implicit request =>
    dataRepository.read(id).map {
      case dataModel if dataModel._id.equals("empty") => BadRequest
      case dataModel => Ok(Json.toJson(dataModel))
      case _ => BadRequest("Unknown error")
    }
  }

  def update(id: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[DataModel] match {
      case JsSuccess(data, _) =>
        dataRepository.update(id, data).map {
          case result if result.getModifiedCount.equals(0l) =>
            dataRepository.read(id).map(foo => println(s"bad $foo"))
            BadRequest
          case result => {println("good" + result.getModifiedCount.equals(0l)); Accepted(Json.toJson(data))}
        }
      case JsError(_) => Future(BadRequest)
    }
  }

  def delete(id: String): Action[AnyContent] = Action.async { implicit request =>
    dataRepository.delete(id).map {
      case 1 => Accepted
      case _ => BadRequest
    }
  }

  def getGoogleBook(search: String, term: String): Action[AnyContent] = Action.async { implicit request =>
    service.getGoogleBook(search = search, term = term).value.map {
      case Right(book) => Ok(Json.toJson(book))
      case Left(error) => NotFound
    }
  }
}
