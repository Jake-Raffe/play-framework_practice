package connectors

import cats.data.EitherT
import models.{APIError, DataModel, GoogleBook}
import play.api.libs.json.{JsError, JsSuccess, OFormat}
import play.api.libs.ws.{WSClient, WSResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class LibraryConnector @Inject()(ws: WSClient) {
  def get[Response](url: String)(implicit rds: OFormat[Response], ec: ExecutionContext): EitherT[Future, APIError, DataModel] = {
    val request = ws.url(url)
    val response = request.get()
    EitherT {
      response
        .map {
          result => {
            result.json.validate[GoogleBook] match {
              case JsSuccess(value, _) =>
                Right(DataModel(value.items.head.id, value.items.head.volumeInfo.title, value.items.head.volumeInfo.description, value.items.head.volumeInfo.pageCount))
              case JsError(errors) =>
                Left(APIError.BadAPIResponse(400, "Could not find book"))
            }}
        }
//        .recover { case _: WSResponse =>
//          Left(APIError.BadAPIResponse(500, "Could not connect"))
//        }
    }
  }
}