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
    applicationService,
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
    val request: FakeRequest[JsValue] = buildPost("/api").withBody[JsValue](Json.toJson(dataModel))
    val emptyRequest: FakeRequest[JsValue] = buildPost("/api").withBody[JsValue](Json.obj())
    val createdResult: Future[Result] = TestApplicationController.create()(request)
    val createdEmptyResult: Future[Result] = TestApplicationController.create()(emptyRequest)
    "create a book in the database" in {
      status(createdResult)(defaultAwaitTimeout) shouldBe Status.CREATED
    }
    "return BadRequest if request body is of wrong format" in {
      status(createdEmptyResult)(defaultAwaitTimeout) shouldBe Status.BAD_REQUEST
    }
  }

  "ApplicationController .read(id: String)" should {
    val request: FakeRequest[JsValue] = buildPost("/api").withBody[JsValue](Json.toJson(dataModel))
    val createdResult: Future[Result] = TestApplicationController.create()(request)
    val readRequest: FakeRequest[AnyContentAsEmpty.type] = buildGet("/api/:id")
    val readResult: Future[Result] = TestApplicationController.read("abcd")(readRequest)
    val readWrongIDResult: Future[Result] = TestApplicationController.read("asdhgahdgakdg")(readRequest)
    "find a book in the database by id" in {
      status(readResult)(defaultAwaitTimeout) shouldBe Status.OK
      contentAsJson(readResult)(defaultAwaitTimeout).as[JsValue] shouldBe Json.toJson(dataModel)
    }
    "return a BadRequest if a book of the ID is not found" in {
      status(readWrongIDResult)(defaultAwaitTimeout) shouldBe Status.BAD_REQUEST
    }
  }

  "ApplicationController .update(id: String, newData: DataModel)" should {
      val request: FakeRequest[JsValue] = buildPost("/api").withBody[JsValue](Json.toJson(dataModel))
      val createdResult: Future[Result] = TestApplicationController.create()(request)
    "find a book in the database by it's ID and replace it with the new book" in {
      val updateRequest: FakeRequest[JsValue] = buildPut("/api/:id").withBody[JsValue](Json.toJson(updatedDataModel))
      val updateResult: Future[Result] = TestApplicationController.update("abcd")(updateRequest)
      status(updateResult)(defaultAwaitTimeout) shouldBe Status.ACCEPTED
      contentAsJson(updateResult)(defaultAwaitTimeout).as[JsValue] shouldBe Json.toJson(updatedDataModel)
    }
    "return NotFound message if wrong ID" in {
      val updateRequest: FakeRequest[JsValue] = buildPut("/api/:id").withBody[JsValue](Json.toJson(updatedDataModel))
      val updateWrongIDResult: Future[Result] = TestApplicationController.update("125")(updateRequest)
      status(updateWrongIDResult)(defaultAwaitTimeout) shouldBe Status.NOT_FOUND
    }
    "return Bad_Request if incorrect Json body" in {
      val emptyRequest: FakeRequest[JsValue] = buildPost("/api").withBody[JsValue](Json.obj())
      val createdEmptyResult: Future[Result] = TestApplicationController.create()(emptyRequest)
      status(createdEmptyResult)(defaultAwaitTimeout) shouldBe Status.BAD_REQUEST
    }
  }

  "ApplicationController .delete(id: String)" should {
      val request: FakeRequest[JsValue] = buildPost("/api").withBody[JsValue](Json.toJson(dataModel))
      val createdResult: Future[Result] = TestApplicationController.create()(request)
    "find a book in the database by id and delete it" in {
      val deleteRequest: FakeRequest[AnyContentAsEmpty.type] = buildDelete("/api/:id")
      val deleteResult: Future[Result] = TestApplicationController.delete("abcd")(deleteRequest)
      status(deleteResult)(defaultAwaitTimeout) shouldBe Status.ACCEPTED
    }
    "return a Bad Request if the id does not exist" in {
      val readEmptyRequest: FakeRequest[AnyContentAsEmpty.type] = buildGet("/read/:id")
      val readEmptyResult: Future[Result] = TestApplicationController.read("5")(readEmptyRequest)
      status(readEmptyResult) shouldBe None
    }
  }

  override def beforeEach(): Unit = repository.deleteAll()
  override def afterEach(): Unit = repository.deleteAll()
}
