package controllers

import baseSpec.BaseSpecWithApplication
import models.DataModel
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.Injecting
import play.api.test.FakeRequest
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.test.Helpers.status

import scala.concurrent.Future

class ApplicationControllerSpec extends BaseSpecWithApplication with Injecting with GuiceOneAppPerSuite {

  val TestApplicationController = new ApplicationController(
    component,
    repository
  )
  private val dataModel: DataModel = DataModel(
    "abcd",
    "test name",
    "test description",
    100
  )

  "ApplicationController .index()" should {
    val result = TestApplicationController.index()(FakeRequest())

    "return TODO" in {
      status(result) shouldBe Status.OK
    }
  }

  "ApplicationController .create()" should {
    "create a book in the database" in {
      val request: FakeRequest[JsValue] = buildPost("/api").withBody[JsValue](Json.toJson(DataModel))
      val createdResult: Future[Result] = TestApplicationController.create(request)
      status(createdResult) shouldBe Status.???
    }
  }

  "ApplicationController .read(id: String)" should {

  }

  "ApplicationController .update(id: String)" should {

  }

  "ApplicationController .delete(id: String)" should {

  }
}
