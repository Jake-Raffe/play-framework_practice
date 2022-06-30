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
    applicationService,
    libraryService,
    executionContext
  )

  private val mockDataModel: DataModel = DataModel(
    "1",
    "Mock Book",
    "This is a data model book for testing",
    100
  )
//  private val mockJsonBook: JsValue = Json.obj(
//    "_id" -> "1",
//    "name" -> "Mock Book",
//    "description" -> "This is a data model book for testing",
//    "numSales" -> 100
//  )
  private val googleBookExample: DataModel = DataModel(
    "bDcJqV-8dlIC",
    "The Black Prism",
    "In a world where magic is tightly controlled, the most powerful man in history must choose between his kingdom and his son - in the first book of the New York Times bestselling Lightbringer series, one of the most popular fantasy epics of the decade. EVERY LIGHT CASTS A SHADOW. Guile is the Prism, the most powerful man in the world. He is high priest and emperor, a man whose power, wit, and charm are all that preserves a tenuous peace. Yet Prisms never last, and Guile knows exactly how long he has left to live. When Guile discovers he has a son, born in a far kingdom after the war that put him in power, he must decide how much he's willing to pay to protect a secret that could tear his world apart. With over four million copies sold, Brent Weeks is one of the fastest-selling fantasy authors of all time. 'Brent Weeks is so good it's beginning to tick me off' Peter V. Brett 'Weeks has a style of immediacy and detail that pulls the reader relentlessly into the story. He doesn't allow you to look away' Robin Hobb 'I was mesmerised from start to finish. Unforgettable characters, a plot that kept me guessing, non-stop action and the kind of in-depth storytelling that makes me admire a writers' work' Terry Brooks 'Weeks has truly cemented his place among the great epic fantasy writers of our time' British Fantasy Society Books by Brent Weeks Night Angel The Way of Shadows Shadow's Edge Beyond the Shadows Perfect Shadow (novella) Lightbringer The Black Prism The Blinding Knife The Broken Eye The Blood Mirror The Burning White",
  480
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
  beforeEach()
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

  "ApplicationController .getGoogleBook()" should {

    "find a book in the database by using a search and term" in {
      val request: FakeRequest[AnyContentAsEmpty.type] = buildGet("/library/google/0316087548/prism")
      val result = integrationTestApplicationController.getGoogleBook("0316087548", "prism")(request)

      status(result) shouldBe Status.OK
      contentAsJson(result) shouldBe Json.toJson(googleBookExample)
    }

    "return a Bad Request if unable to find a book in the database by using a search and term" in {
      val badRequest: FakeRequest[AnyContentAsEmpty.type] = buildGet("/library/google/9999999999/rings")
      val badResult= integrationTestApplicationController.getGoogleBook("9999999999", "rings")(badRequest)


      status(badResult) shouldBe Status.INTERNAL_SERVER_ERROR
      contentAsJson(badResult) shouldBe Json.toJson("Bad response from upstream; Status: 400, Reason: Could not find book")
    }
  }

//  override def beforeEach(): Unit = {  }
  override def beforeEach(): Unit = repository.deleteAll()
//  override def afterEach(): Unit = repository.deleteAll()
}
