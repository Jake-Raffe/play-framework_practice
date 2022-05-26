package controllers

import other.DataRepository
import play.api.mvc.{BaseController, ControllerComponents}

import javax.inject.{Inject, Singleton}


@Singleton
class ApplicationController @Inject()(val controllerComponents: ControllerComponents, val repository: DataRepository) extends BaseController {

  def index() = TODO
  def create() = TODO
  def read(id: String) = TODO
  def update(id: String) = TODO
  def delete(id: String) = TODO
}
