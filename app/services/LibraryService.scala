package services

import connectors.LibraryConnector
import models.{APIError, DataModel}
import play.api.libs.json.OFormat
import play.mvc.BodyParser.Json
import cats.data.EitherT

import java.awt.print.Book
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LibraryService @Inject()(connector: LibraryConnector){

  def getGoogleBook(urlOverride: Option[String] = None, search: String, term: String)(implicit ec: ExecutionContext): EitherT[Future, APIError, DataModel] =
   connector.get[DataModel](urlOverride.getOrElse(s"https://www.googleapis.com/books/v1/volumes?q=$search%$term"))


}

