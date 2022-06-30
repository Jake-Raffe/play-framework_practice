package models

import play.api.libs.json.{Json, OFormat}

case class VolumeInfo(title: String, description: String, pageCount: Int)

case class Item(id: String, volumeInfo: VolumeInfo)

case class GoogleBook(items: List[Item])

object GoogleBook {
  implicit val formats: OFormat[GoogleBook] = Json.format[GoogleBook]
}
object Item {
  implicit val formats: OFormat[Item] = Json.format[Item]
}

object VolumeInfo {
  implicit val formats: OFormat[VolumeInfo] = Json.format[VolumeInfo]
}
