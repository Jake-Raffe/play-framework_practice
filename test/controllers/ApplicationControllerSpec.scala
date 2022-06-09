package controllers

import baseSpec.BaseSpecWithApplication
import models.DataModel
import play.api.test.{FakeRequest, Injecting}
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContentAsEmpty, Request, Result}
import play.api.test.Helpers.{await, contentAsJson, defaultAwaitTimeout, status}
import repositories.DataRepository
import uk.gov.hmrc.mongo.MongoComponent

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
    dataModel._id,
    "update name",
    "update description",
    100
  )

  "ApplicationController .index()" should {
    beforeEach()
    val result = TestApplicationController.index()(FakeRequest())
    "return status OK" in {
      status(result)(defaultAwaitTimeout) shouldBe Status.OK
    }
  }

  "ApplicationController .create()" should {
    "create a book in the database" in {
      beforeEach()
      val request: FakeRequest[JsValue] = buildPost("/api").withBody[JsValue](Json.toJson(dataModel))
      val createdResult: Future[Result] = TestApplicationController.create()(request)
      println(Json.prettyPrint(contentAsJson(createdResult)))
      status(createdResult)(defaultAwaitTimeout) shouldBe Status.CREATED
      contentAsJson(createdResult)(defaultAwaitTimeout).as[JsValue] shouldBe Json.toJson(dataModel)
    }
    "return BadRequest if request body is of wrong format" in {
      val emptyRequest: FakeRequest[JsValue] = buildPost("/api").withBody[JsValue](Json.obj())
      val createdEmptyResult: Future[Result] = TestApplicationController.create()(emptyRequest)
      status(createdEmptyResult)(defaultAwaitTimeout) shouldBe Status.BAD_REQUEST
    }
  }

  "ApplicationController .read(id: String)" should {
    beforeEach()
    val readRequest: FakeRequest[AnyContentAsEmpty.type] = buildGet("/api/:id")
    "find a book in the database by id" in {
      val request: FakeRequest[JsValue] = buildPost("/api").withBody[JsValue](Json.toJson(dataModel))
      TestApplicationController.create()(request)
      val readResult: Future[Result] = TestApplicationController.read("abcd")(readRequest)
      status(readResult)(defaultAwaitTimeout) shouldBe Status.OK
      contentAsJson(readResult)(defaultAwaitTimeout).as[JsValue] shouldBe Json.toJson(dataModel)
    }
    "return a BadRequest if a book of the ID is not found" in {
      val readWrongIDResult: Future[Result] = TestApplicationController.read("asdhgahdgakdg")(readRequest)
      status(readWrongIDResult)(defaultAwaitTimeout) shouldBe Status.BAD_REQUEST
    }
  }

  "ApplicationController .update(id: String, newData: DataModel)" should {

    "find a book in the database by it's ID and replace it with the new book" in {
      beforeEach()
      val request: FakeRequest[JsValue] = buildPost("/api").withBody[JsValue](Json.toJson(dataModel))
      val createdRequest = TestApplicationController.create()(request)
      await(createdRequest)
      val updateRequest: FakeRequest[JsValue] = buildPut("/api/:id").withBody[JsValue](Json.toJson(updatedDataModel))
      val updateResult: Future[Result] = TestApplicationController.update("abcd")(updateRequest)

      status(updateResult)(defaultAwaitTimeout) shouldBe Status.ACCEPTED
      contentAsJson(updateResult)(defaultAwaitTimeout).as[JsValue] shouldBe Json.toJson(updatedDataModel)
    }

    "return NotFound message if wrong ID" in {
      beforeEach()
      val request: FakeRequest[JsValue] = buildPost("/api").withBody[JsValue](Json.toJson(dataModel))
      TestApplicationController.create()(request)
      val updateRequest: FakeRequest[JsValue] = buildPut("/api/:id").withBody[JsValue](Json.toJson(updatedDataModel))
      val updateWrongIDResult: Future[Result] = TestApplicationController.update("125")(updateRequest)
      status(updateWrongIDResult)(defaultAwaitTimeout) shouldBe Status.BAD_REQUEST
    }

    "return Bad_Request if incorrect Json body" in {
      beforeEach()
      val request: FakeRequest[JsValue] = buildPost("/api").withBody[JsValue](Json.toJson(dataModel))
      TestApplicationController.create()(request)
      val emptyRequest: FakeRequest[JsValue] = buildPost("/api").withBody[JsValue](Json.obj())
      val createdEmptyResult: Future[Result] = TestApplicationController.create()(emptyRequest)
      status(createdEmptyResult)(defaultAwaitTimeout) shouldBe Status.BAD_REQUEST
    }
  }

  "ApplicationController .delete(id: String)" should {
    "find a book in the database by id and delete it" in {
      beforeEach()
      val request: FakeRequest[JsValue] = buildPost("/api").withBody[JsValue](Json.toJson(dataModel))
      val createdResult = TestApplicationController.create()(request)
      await(createdResult)
      val deleteRequest: FakeRequest[AnyContentAsEmpty.type] = buildDelete("/api/:id")
      val deleteResult: Future[Result] = TestApplicationController.delete("abcd")(deleteRequest)
      status(deleteResult)(defaultAwaitTimeout) shouldBe Status.ACCEPTED
    }
    "return a Bad Request if the id does not exist" in {
      val readEmptyRequest: FakeRequest[AnyContentAsEmpty.type] = buildGet("/read/:id")
      val readEmptyResult: Future[Result] = TestApplicationController.read("5")(readEmptyRequest)
      status(readEmptyResult) shouldBe Status.BAD_REQUEST
    }
  }

  override def beforeEach(): Unit = repository.deleteAll()
//  override def afterEach(): Unit = repository.deleteAll()
}
