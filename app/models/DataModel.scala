package models

import org.mongodb.scala.bson.BsonDocument
import play.api.libs.json.{JsValue, Json, OFormat, Writes}


case class DataModel(_id: String,
                     title: String,
                     description: String,
                     pageCount: Int)

object DataModel {
  implicit val formats: OFormat[DataModel] = Json.format[DataModel]
//  implicit object BookBSONReader {
//    def read(doc: BsonDocument): DataModel = {
//      DataModel(
//        doc.getString("id").toString, //.get("id").toString, //or this not sure if either work
//        doc.get("title").toString,
//        doc.get("description").toString,
//        doc.getInt32("pageNumber").intValue())
//    }
//  }
//  implicit val implicitWrites = new Writes[DataModel] {
//    def writes(updated: DataModel): JsValue = {
//      Json.obj(
//        "_id" -> updated._id,
//        "name" -> updated.name,
//        "description" -> updated.description,
//        "numSales" -> updated.numSales
//      )
//    }
//  }
}