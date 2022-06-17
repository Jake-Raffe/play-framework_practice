package controllers

import baseSpec.BaseSpecWithApplication
import cats.data.EitherT
import models.APIError.BadAPIResponse
import models.{APIError, DataModel, UpdateField}
import org.junit.Test.None
import org.scalamock.matchers.Matchers
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.test.{FakeRequest, Injecting}
import play.api.http.Status
import play.api.libs.json.{JsValue, Json, OFormat}
import play.api.mvc.Results.Created
import play.api.mvc.{Action, AnyContentAsEmpty, Request, Result}
import play.api.test.Helpers.{await, contentAsJson, defaultAwaitTimeout, status}
import repositories.DataRepository
import services.{ApplicationService, LibraryService}
import uk.gov.hmrc.mongo.MongoComponent

import scala.concurrent.{ExecutionContext, Future}

class ApplicationControllerSpec extends BaseSpecWithApplication with MockFactory with ScalaFutures with Matchers {
// with Injecting with GuiceOneAppPerSuite {

  override implicit lazy val app: Application = fakeApplication()
  val integrationTestApplicationController = new ApplicationController(
    component,
//    repository,
    applicationService,
    libraryService,
    executionContext
  )
  val unitTestApplicationController = new ApplicationController(
    component,
    mockApplicationService,
    mockLibraryService,
    executionContext
  )

  val mockApplicationService = mock[ApplicationService]
  val mockLibraryService = mock[LibraryService]

  private val mockDataModel: DataModel = DataModel(
    "1",
    "Mock Book",
    "This is a data model book for testing",
    100
  )
  private val mockJsonBook: JsValue = Json.obj(
    "_id" -> "1",
    "name" -> "Mock Book",
    "description" -> "This is a data model book for testing",
    "numSales" -> 100
  )

  private val updatedMockDataModel: DataModel = DataModel(
    mockDataModel._id,
    "update name",
    "update description",
    100
  )
  private val editedMockDataModel: DataModel = DataModel(
    mockDataModel._id,
    "Mock Book",
    "this is the edited description",
    100
  )
  private val updateField: UpdateField = UpdateField(
    mockDataModel._id,
    "description",
    "this is the edited description"
  )

  // UNIT TESTING

//  "ApplicationController .create()" should {
//    "return Created with the book that has been added to the database" in {
//      val request: FakeRequest[JsValue] = buildPost("/api").withBody[JsValue](mockJsonBook)
//      (mockApplicationService.create(_: Request[JsValue]) )
//        .expects(request)
//        .returning(Future[Either[APIError, DataModel]](Right(mockDataModel)))
//        .once()
//      whenReady(unitTestApplicationController.create()(request)) { result =>
//        result shouldBe Created(Json.toJson(mockDataModel))
//      }
//    }}
//
//    "return a BadAPIResponse if the request body is of the wrong format" in {
//      val badRequest: FakeRequest[JsValue] = buildPost("/api").withBody[JsValue](Json.obj())
//      (mockApplicationService.create _)
//        .expects(badRequest)
//        .returning(Future[Either[APIError, DataModel]](Left(BadAPIResponse(400, "Unable to complete request"))))
//        .once()
//      whenReady(unitTestApplicationController.create()) { result =>
//        result shouldBe BadAPIResponse(400, "Unable to complete request")
//      }
//    }
//  }


  // INTEGRATION TESTING

  // Index good
  "ApplicationController .index()" should {
    val result = integrationTestApplicationController.index()(FakeRequest())
    "return status OK" in {
      status(result)(defaultAwaitTimeout) shouldBe Status.OK
    }
  }

  // Create good
  "ApplicationController .create()" should {
    "create a book in the database" in {
      val request: FakeRequest[JsValue] = buildPost("/api").withBody[JsValue](Json.toJson(mockDataModel))
      val createdResult: Future[Result] = integrationTestApplicationController.create()(request)
      println(Json.prettyPrint(contentAsJson(createdResult)))
      status(createdResult)(defaultAwaitTimeout) shouldBe Status.CREATED
      contentAsJson(createdResult)(defaultAwaitTimeout).as[JsValue] shouldBe Json.toJson(mockDataModel)
    }
    "return BadRequest if request body is of wrong format" in {
      val emptyRequest: FakeRequest[JsValue] = buildPost("/api").withBody[JsValue](Json.obj())
      val createdEmptyResult: Future[Result] = integrationTestApplicationController.create()(emptyRequest)
      status(createdEmptyResult)(defaultAwaitTimeout) shouldBe Status.INTERNAL_SERVER_ERROR
      contentAsJson(createdEmptyResult)(defaultAwaitTimeout) shouldBe Json.toJson("Bad response from upstream; Status: 415, Reason: Unable to validate request body format")
    }
  }

  // ReadID good
  "ApplicationController .readId(id: String)" should {

    "find a book in the database by id" in {
      val request: FakeRequest[JsValue] = buildPost("/api").withBody[JsValue](Json.toJson(mockDataModel))
      integrationTestApplicationController.create()(request)

      val readRequest: FakeRequest[AnyContentAsEmpty.type] = buildGet("/api/id/:id")
      val readResult: Future[Result] = integrationTestApplicationController.readId("1")(readRequest)
      status(readResult)(defaultAwaitTimeout) shouldBe Status.OK
      contentAsJson(readResult)(defaultAwaitTimeout).as[JsValue] shouldBe Json.toJson(mockDataModel)
    }
    "return a NotFound if a book of the ID is not found" in {
      val readRequest: FakeRequest[AnyContentAsEmpty.type] = buildGet("/api/id/:id")
      val readWrongIDResult: Future[Result] = integrationTestApplicationController.readId("22")(readRequest)
      status(readWrongIDResult)(defaultAwaitTimeout) shouldBe Status.INTERNAL_SERVER_ERROR
      contentAsJson(readWrongIDResult)(defaultAwaitTimeout) shouldBe Json.toJson("Bad response from upstream; Status: 404, Reason: Unable to find book of ID: 22")
    }
  }

  // ReadName good
  "ApplicationController .readName(name: String)" should {
    val readRequest: FakeRequest[AnyContentAsEmpty.type] = buildGet("/api/name/:name")

    "find a book in the database by name" in {
      val request: FakeRequest[JsValue] = buildPost("/api").withBody[JsValue](Json.toJson(mockDataModel))
      integrationTestApplicationController.create()(request)

      val readResult: Future[Result] = integrationTestApplicationController.readName("Mock Book")(readRequest)
      status(readResult)(defaultAwaitTimeout) shouldBe Status.OK
      contentAsJson(readResult)(defaultAwaitTimeout).as[JsValue] shouldBe Json.toJson(mockDataModel)
    }
    "return a NotFound if a book of the name is not found" in {
      val readWrongIDResult: Future[Result] = integrationTestApplicationController.readName("Wrong name book")(readRequest)
      status(readWrongIDResult)(defaultAwaitTimeout) shouldBe Status.INTERNAL_SERVER_ERROR
      contentAsJson(readWrongIDResult)(defaultAwaitTimeout) shouldBe Json.toJson("Bad response from upstream; Status: 404, Reason: Unable to find book of name: Wrong name book")
    }
  }

  // Update good
  "ApplicationController .update(id: String)" should {

    "find a book in the database by it's ID and replace it with the new book" in {
      beforeEach()
      val request: FakeRequest[JsValue] = buildPost("/api").withBody[JsValue](Json.toJson(mockDataModel))
      val createdRequest = integrationTestApplicationController.create()(request)
      await(createdRequest)
      val updateRequest: FakeRequest[JsValue] = buildPut("/api/:id").withBody[JsValue](Json.toJson(updatedMockDataModel))
      val updateResult: Future[Result] = integrationTestApplicationController.update("1")(updateRequest)

      status(updateResult)(defaultAwaitTimeout) shouldBe Status.ACCEPTED
      contentAsJson(updateResult)(defaultAwaitTimeout).as[JsValue] shouldBe Json.toJson(updatedMockDataModel)
    }

    "return BadAPIResponse message if wrong ID" in {
      val request: FakeRequest[JsValue] = buildPost("/api").withBody[JsValue](Json.toJson(mockDataModel))
      integrationTestApplicationController.create()(request)
      val updateRequest: FakeRequest[JsValue] = buildPut("/api/:id").withBody[JsValue](Json.toJson(updatedMockDataModel))
      val updateWrongIDResult: Future[Result] = integrationTestApplicationController.update("125")(updateRequest)
      status(updateWrongIDResult)(defaultAwaitTimeout) shouldBe Status.INTERNAL_SERVER_ERROR
      contentAsJson(updateWrongIDResult)(defaultAwaitTimeout) shouldBe Json.toJson("Bad response from upstream; Status: 400, Reason: Unable to update book of ID: 125")
    }

    "return BadRequest if incorrect Json body" in {
      val request: FakeRequest[JsValue] = buildPost("/api").withBody[JsValue](Json.toJson(mockDataModel))
      integrationTestApplicationController.create()(request)
      val emptyUpdateRequest: FakeRequest[JsValue] = buildPut("/api/:id").withBody[JsValue](Json.obj())
      val emptyUpdateResult: Future[Result] = integrationTestApplicationController.update("1")(emptyUpdateRequest)
      status(emptyUpdateResult)(defaultAwaitTimeout) shouldBe Status.BAD_REQUEST
    }
  }

  // Edit good
  "ApplicationController .edit(id: String)" should {

    "find a book in the database by it's ID and replace a field with an edit before returning the updated book" in {
      beforeEach()
      val request: FakeRequest[JsValue] = buildPost("/api").withBody[JsValue](Json.toJson(mockDataModel))
      val createdRequest = integrationTestApplicationController.create()(request)
//      await(createdRequest)
      val editRequest: FakeRequest[JsValue] = buildPut("/api/edit/:id").withBody[JsValue](Json.toJson(updateField))
      val editResult: Future[Result] = integrationTestApplicationController.edit("1")(editRequest)

      await(editResult)
      status(editResult)(defaultAwaitTimeout) shouldBe Status.ACCEPTED
      contentAsJson(editResult)(defaultAwaitTimeout).as[JsValue] shouldBe Json.toJson(editedMockDataModel)
    }

    "return BadAPIResponse message if wrong ID" in {
      val request: FakeRequest[JsValue] = buildPost("/api").withBody[JsValue](Json.toJson(mockDataModel))
      integrationTestApplicationController.create()(request)
      val updateRequest: FakeRequest[JsValue] = buildPut("/api/edit/:id").withBody[JsValue](Json.toJson(updateField))
      val updateWrongIDResult: Future[Result] = integrationTestApplicationController.edit("125")(updateRequest)
      status(updateWrongIDResult)(defaultAwaitTimeout) shouldBe Status.INTERNAL_SERVER_ERROR
      contentAsJson(updateWrongIDResult)(defaultAwaitTimeout) shouldBe Json.toJson("Bad response from upstream; Status: 400, Reason: Unable to edit book of ID: 125")
    }

    "return BadRequest if incorrect Json body" in {
      val request: FakeRequest[JsValue] = buildPost("/api").withBody[JsValue](Json.toJson(mockDataModel))
      integrationTestApplicationController.create()(request)
      val emptyEditRequest: FakeRequest[JsValue] = buildPut("/api/edit/:id").withBody[JsValue](Json.obj())
      val emptyEditResult: Future[Result] = integrationTestApplicationController.edit("1")(emptyEditRequest)
      status(emptyEditResult)(defaultAwaitTimeout) shouldBe Status.BAD_REQUEST
    }
  }

  // Delete good
  "ApplicationController .delete(id: String)" should {
    "find a book in the database by id and delete it" in {
      beforeEach()
      val request: FakeRequest[JsValue] = buildPost("/api").withBody[JsValue](Json.toJson(mockDataModel))
      val createdResult = integrationTestApplicationController.create()(request)
      await(createdResult)
      val deleteRequest: FakeRequest[AnyContentAsEmpty.type] = buildDelete("/api/:id")
      val deleteResult: Future[Result] = integrationTestApplicationController.delete("1")(deleteRequest)
      status(deleteResult)(defaultAwaitTimeout) shouldBe Status.ACCEPTED
    }
    "return a BadAPIResponse if the ID does not exist" in {
      val deleteWrongIdRequest: FakeRequest[AnyContentAsEmpty.type] = buildDelete("/api/:id")
      val deleteWrongIdResult: Future[Result] = integrationTestApplicationController.delete("5")(deleteWrongIdRequest)
      status(deleteWrongIdResult)(defaultAwaitTimeout) shouldBe Status.INTERNAL_SERVER_ERROR
      contentAsJson(deleteWrongIdResult)(defaultAwaitTimeout) shouldBe Json.toJson("Bad response from upstream; Status: 400, Reason: Unable to delete book of ID: 5")
    }
  }

//  override def beforeEach(): Unit = {  }
  override def beforeEach(): Unit = repository.deleteAll()
//  override def afterEach(): Unit = repository.deleteAll()
}
