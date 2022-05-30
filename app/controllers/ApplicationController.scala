package controllers

import models.DataModel
import org.mongodb.scala.result
import play.api.http.Status.ACCEPTED
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json, Writes}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import repositories.DataRepository

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}


@Singleton
class ApplicationController @Inject()(val controllerComponents: ControllerComponents,
                                      val dataRepository: DataRepository,
                                      implicit val ec: ExecutionContext
                                     ) extends BaseController {

  def index(): Action[AnyContent] = Action.async { implicit request =>
    val books: Future[Seq[DataModel]] = dataRepository.collection.find().toFuture()
    books.map(items => Json.toJson(items)).map(result => Ok(result))
  }

  def create(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[DataModel] match {
      case JsSuccess(dataModel, _) =>
        dataRepository.create(dataModel).map(_ => Created)
      case JsError(_) => Future(BadRequest)
    }
  }

  def read(id: String): Action[AnyContent] = Action.async { implicit request =>
    val book = dataRepository.read(id)
    book.map(item => Json.toJson(item)).map(result => Ok(result))
  }

  def update(id: String, newData: DataModel): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[DataModel] match {
      case JsSuccess(data, _) =>
        dataRepository.update(id, data)
        dataRepository.read(id) map {
          case data => Accepted(Json.toJson(DataModel.implicitWrites.writes(data)))
        }
      case JsError(_) => Future(BadRequest)
    }
  }

  def delete(id: String): Action[AnyContent] = Action.async { implicit request =>
    dataRepository.delete(id) match {
      case JsSuccess(_, _) => Future(Accepted)
      case JsError(_) => Future(BadRequest)
    }
  }
}
