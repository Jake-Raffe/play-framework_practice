package controllers

import baseSpec.BaseSpecWithApplication
import models.DataModel
import play.api.test.{FakeRequest, Injecting}
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContentAsEmpty, Result}
import play.api.test.Helpers.{contentAsJson, defaultAwaitTimeout, status}

import scala.concurrent.Future

class ApplicationControllerSpec extends BaseSpecWithApplication {
// with Injecting with GuiceOneAppPerSuite {

  val TestApplicationController = new ApplicationController(
    component,
    repository,
    executionContext
  )

  private val dataModel: DataModel = DataModel(
    "abcd",
    "test name",
    "test description",
    100
  )
  private val updatedDataModel: DataModel = DataModel(
    "abcd",
    "update name",
    "update description",
    100
  )

  "ApplicationController .index()" should {
    val result = TestApplicationController.index()(FakeRequest())
    "return status OK" in {
      status(result)(defaultAwaitTimeout) shouldBe Status.OK
    }
  }

  "ApplicationController .create()" should {
    "create a book in the database" in {
      val request: FakeRequest[JsValue] = buildPost("/create").withBody[JsValue](Json.toJson(dataModel))
      val createdResult: Future[Result] = TestApplicationController.create()(request)
      status(createdResult)(defaultAwaitTimeout) shouldBe Status.CREATED
    }
  }

  "ApplicationController .read(id: String)" should {
    "find a book in the database by id" in {
      val request: FakeRequest[JsValue] = buildPost("/create").withBody[JsValue](Json.toJson(dataModel))
      val createdResult: Future[Result] = TestApplicationController.create()(request)
      status(createdResult)(defaultAwaitTimeout) shouldBe Status.CREATED
      val readRequest: FakeRequest[AnyContentAsEmpty.type] = buildGet("/read/:id")
      val readResult: Future[Result] = TestApplicationController.read("abcd")(readRequest)
      status(readResult)(defaultAwaitTimeout) shouldBe Status.OK
      contentAsJson(readResult)(defaultAwaitTimeout).as[JsValue] shouldBe Json.toJson(dataModel)
    }
  }

  "ApplicationController .update(id: String, newData: DataModel)" should {
    "find a book in the database by it's ID and replace it with the new book" in {
      val request: FakeRequest[JsValue] = buildPost("/create").withBody[JsValue](Json.toJson(dataModel))
      val createdResult: Future[Result] = TestApplicationController.create()(request)
      status(createdResult)(defaultAwaitTimeout) shouldBe Status.CREATED
      val updateRequest: FakeRequest[JsValue] = buildPut("/update/:id").withBody[JsValue](Json.toJson(updatedDataModel))
      val updateResult: Future[Result] = TestApplicationController.update("abcd")(updateRequest)
      status(updateResult)(defaultAwaitTimeout) shouldBe Status.ACCEPTED
      contentAsJson(updateResult)(defaultAwaitTimeout).as[JsValue] shouldBe Json.toJson(updatedDataModel)
    }
  }

  "ApplicationController .delete(id: String)" should {
    "find a book in the database by id and delete it" in {
      val request: FakeRequest[JsValue] = buildPost("/create").withBody[JsValue](Json.toJson(dataModel))
      val createdResult: Future[Result] = TestApplicationController.create()(request)
      status(createdResult)(defaultAwaitTimeout) shouldBe Status.CREATED
      val deleteRequest: FakeRequest[AnyContentAsEmpty.type] = buildDelete("/delete/:id")
      val deleteResult: Future[Result] = TestApplicationController.delete("abcd")(deleteRequest)
      status(deleteResult)(defaultAwaitTimeout) shouldBe Status.ACCEPTED
    }
//    "return a Bad Request if the id does not exist" in {
//      beforeEach()
//      val readEmptyRequest: FakeRequest[AnyContentAsEmpty.type] = buildGet("/read/:id")
//      val readEmptyResult: Future[Result] = TestApplicationController.read("5")(readEmptyRequest)
//      status(readEmptyResult) shouldBe Status.BAD_REQUEST
//    }
  }

  override def beforeEach(): Unit = repository.deleteAll()
  override def afterEach(): Unit = repository.deleteAll()
}
