package models

import play.api.libs.json.{JsValue, Json, OFormat, Writes}


case class DataModel(_id: String,
                     name: String,
                     description: String,
                     numSales: Int)

object DataModel {
  implicit val formats: OFormat[DataModel] = Json.format[DataModel]
  implicit val implicitWrites = new Writes[DataModel] {
    def writes(updated: DataModel): JsValue = {
      Json.obj(
        "_id" -> updated._id,
        "name" -> updated.name,
        "description" -> updated.description,
        "numSales" -> updated.numSales
      )
    }
  }
}