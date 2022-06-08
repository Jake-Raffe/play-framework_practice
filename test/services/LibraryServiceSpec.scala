package services

import baseSpec.BaseSpec
import connectors.LibraryConnector
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.{JsValue, Json, OFormat}
import play.api.http.Status
import scala.concurrent.{ExecutionContext, Future}
import models.DataModel
import play.api.test.{FakeRequest, Injecting}
import play.api.mvc.{Action, AnyContentAsEmpty, Result}
import play.api.test.Helpers.{contentAsJson, defaultAwaitTimeout, status}


class LibraryServiceSpec extends BaseSpec with MockFactory with ScalaFutures with GuiceOneAppPerSuite {

  val mockConnector = mock[LibraryConnector]
  implicit val executionContext: ExecutionContext = app.injector.instanceOf[ExecutionContext]
  val testService = new LibraryService(mockConnector)

  val gameOfThrones: JsValue = Json.obj(
    "_id" -> "someId",
    "name" -> "A Game Of Thrones",
    "description" -> "Good book",
    "numSales" -> 100
  )

  "getGoogleBook" should {
    val url: String = "testUrl"

//    "return a book" in {
//      (mockConnector.get[Response](_: String)(_: OFormat[Response], _: ExecutionContext))
//        .expects(url, *, *)
//        .returning(Future(gameOfThrones.as[JsValue]))
//        .once()
//      whenReady(testService.getGoogleBook(urlOverride = Some(url), search = "", term = "")) { result =>
//        result shouldBe gameOfThrones
//      }
//    }
//    "return an error" in {
//      (mockConnector.get[Response](_: String)(_: OFormat[Response], _: ExecutionContext))
//        .expects(url, *, *)
//        .returning(Future(Status.NOT_FOUND))
//        .once()
//      whenReady(testService.getGoogleBook(urlOverride = Some(url), search = "", term = "")) { result =>
//        result shouldBe Status.NOT_FOUND
//      }
//    }
    }
}
