package controllers

import play.api.mvc.{BaseController, ControllerComponents, Action, Request, AnyContent}

import javax.inject._
import scala.concurrent.Future



@Singleton
class HomeController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {


  def index(): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    Future.successful(Ok(views.html.index()))
  }
}
