package controllers

import baseSpec.BaseSpecWithApplication
import models.APIError.BadAPIResponse
import models.{APIError, DataModel, UpdateField}
import org.scalamock.scalatest.MockFactory
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContentAsEmpty, Request, Result}
import play.api.mvc.Results.{Accepted, Created, Ok}
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, contentAsJson, defaultAwaitTimeout, status}
import services.{ApplicationService, LibraryService}

import scala.concurrent.Future

class ApplicationControllerUnitSpec extends BaseSpecWithApplication with MockFactory{

  val mockApplicationService = mock[ApplicationService]
  val mockLibraryService = mock[LibraryService]

  val unitTestApplicationController = new ApplicationController(
    component,
    mockApplicationService,
    mockLibraryService,
    executionContext
  )

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

  "ApplicationController .create()" should {
      "return Created with the book that has been added to the database" in {
        val request: FakeRequest[JsValue] = buildPost("/api").withBody[JsValue](Json.toJson(mockDataModel))
        (mockApplicationService.create(_: Request[JsValue]))
          .expects(request)
          .returning(Future(Right(mockDataModel)))
          .once()
        val createdResult: Future[Result] = unitTestApplicationController.create()(request)

        status(createdResult)(defaultAwaitTimeout) shouldBe Status.CREATED
        contentAsJson(createdResult).as[DataModel] shouldBe mockDataModel
      }

      "return a BadAPIResponse if the request body is of the wrong format" in {
        val badRequest: FakeRequest[JsValue] = buildPost("/api").withBody[JsValue](Json.obj())
        (mockApplicationService.create(_: Request[JsValue]))
          .expects(badRequest)
          .returning(Future(Left(APIError.BadAPIResponse(415, "Unable to validate request body format"))))
          .once()
        val badCreatedResult: Future[Result] = unitTestApplicationController.create()(badRequest)

        status(badCreatedResult)(defaultAwaitTimeout) shouldBe Status.INTERNAL_SERVER_ERROR
        contentAsJson(badCreatedResult)(defaultAwaitTimeout) shouldBe Json.toJson("Bad response from upstream; Status: 415, Reason: Unable to validate request body format")
      }
    }

  "ApplicationController .readId(id: String)" should {

    "find a book in the database by id" in {
      val readRequest: FakeRequest[AnyContentAsEmpty.type] = buildGet("/api/id/:id")
      (mockApplicationService.read(_: String, _: String))
        .expects("ID", "1")
        .returning(Future(Right(Ok(Json.toJson(mockDataModel)))))
        .once()
      val readResult: Future[Result] = unitTestApplicationController.readId("1")(readRequest)

      status(readResult)(defaultAwaitTimeout) shouldBe Status.OK
      contentAsJson(readResult)(defaultAwaitTimeout).as[JsValue] shouldBe Json.toJson(mockDataModel)
    }
    "return a NotFound if a book of the ID is not found" in {
      val readRequest: FakeRequest[AnyContentAsEmpty.type] = buildGet("/api/id/:id")
      (mockApplicationService.read(_: String, _: String))
        .expects("ID", "22")
        .returning(Future(Left(BadAPIResponse(404, "Unable to find book of ID: 22"))))
        .once()
      val readWrongIDResult: Future[Result] = unitTestApplicationController.readId("22")(readRequest)

      status(readWrongIDResult)(defaultAwaitTimeout) shouldBe Status.INTERNAL_SERVER_ERROR
      contentAsJson(readWrongIDResult)(defaultAwaitTimeout) shouldBe Json.toJson("Bad response from upstream; Status: 404, Reason: Unable to find book of ID: 22")
    }
  }

  "ApplicationController .readName(name: String)" should {

    "find a book in the database by name" in {
      val readRequest: FakeRequest[AnyContentAsEmpty.type] = buildGet("/api/name/:name")
      (mockApplicationService.read(_: String, _: String))
        .expects("name", "Mock Book")
        .returning(Future(Right(Ok(Json.toJson(mockDataModel)))))
        .once()
      val readResult: Future[Result] = unitTestApplicationController.readName("Mock Book")(readRequest)

      status(readResult)(defaultAwaitTimeout) shouldBe Status.OK
      contentAsJson(readResult)(defaultAwaitTimeout).as[JsValue] shouldBe Json.toJson(mockDataModel)
    }
    "return a NotFound if a book of the name is not found" in {
      val readRequest: FakeRequest[AnyContentAsEmpty.type] = buildGet("/api/name/:name")
      (mockApplicationService.read(_: String, _: String))
        .expects("name", "Wrong name book")
        .returning(Future(Left(BadAPIResponse(404, "Unable to find book of name: Wrong name book"))))
        .once()
      val readWrongNameResult: Future[Result] = unitTestApplicationController.readName("Wrong name book")(readRequest)

      status(readWrongNameResult)(defaultAwaitTimeout) shouldBe Status.INTERNAL_SERVER_ERROR
      contentAsJson(readWrongNameResult)(defaultAwaitTimeout) shouldBe Json.toJson("Bad response from upstream; Status: 404, Reason: Unable to find book of name: Wrong name book")
    }
  }

  "ApplicationController .update(id: String)" should {

    "find a book in the database by it's ID and replace it with the new book" in {
      val updateRequest: FakeRequest[JsValue] = buildPut("/api/:id").withBody[JsValue](Json.toJson(updatedMockDataModel))
      (mockApplicationService.update(_: String, _: DataModel))
        .expects("1", updatedMockDataModel)
        .returning(Future(Right(Accepted(Json.toJson(updatedMockDataModel)))))
        .once()
      val updateResult: Future[Result] = unitTestApplicationController.update("1")(updateRequest)

      status(updateResult)(defaultAwaitTimeout) shouldBe Status.ACCEPTED
      contentAsJson(updateResult)(defaultAwaitTimeout).as[JsValue] shouldBe Json.toJson(updatedMockDataModel)
    }

    "return BadAPIResponse message if wrong ID" in {
      val updateRequest: FakeRequest[JsValue] = buildPut("/api/:id").withBody[JsValue](Json.toJson(updatedMockDataModel))
      (mockApplicationService.update(_: String, _: DataModel))
        .expects("125", updatedMockDataModel)
        .returning(Future(Left(APIError.BadAPIResponse(400, "Unable to update book of ID: 125"))))
        .once()
      val updateResult: Future[Result] = unitTestApplicationController.update("125")(updateRequest)

      status(updateResult)(defaultAwaitTimeout) shouldBe Status.INTERNAL_SERVER_ERROR
      contentAsJson(updateResult)(defaultAwaitTimeout).as[JsValue] shouldBe Json.toJson("Bad response from upstream; Status: 400, Reason: Unable to update book of ID: 125")
    }

    "return BadRequest if incorrect Json body" in {
      val emptyUpdateRequest: FakeRequest[JsValue] = buildPut("/api/:id").withBody[JsValue](Json.obj())
      val emptyUpdateResult: Future[Result] = unitTestApplicationController.update("1")(emptyUpdateRequest)

      status(emptyUpdateResult)(defaultAwaitTimeout) shouldBe Status.BAD_REQUEST
    }
  }

  "ApplicationController .edit(id: String)" should {

    "find a book in the database by it's ID and replace a field with an edit before returning the updated book" in {
      val updateRequest: FakeRequest[JsValue] = buildPut("/api/edit/:id").withBody[JsValue](Json.toJson(updateField))
      (mockApplicationService.edit(_: String, _: UpdateField))
        .expects("1", updateField)
        .returning(Future(Right(Accepted(Json.toJson(editedMockDataModel)))))
        .once()
      val updateResult: Future[Result] = unitTestApplicationController.edit("1")(updateRequest)

      status(updateResult)(defaultAwaitTimeout) shouldBe Status.ACCEPTED
      contentAsJson(updateResult)(defaultAwaitTimeout).as[JsValue] shouldBe Json.toJson(editedMockDataModel)
    }

    "return BadAPIResponse message if wrong ID" in {
      val updateRequest: FakeRequest[JsValue] = buildPut("/api/edit/:id").withBody[JsValue](Json.toJson(updateField))
      (mockApplicationService.edit(_: String, _: UpdateField))
        .expects("125", updateField)
        .returning(Future(Left(APIError.BadAPIResponse(400, "Unable to edit book of ID: 125"))))
        .once()
      val updateResult: Future[Result] = unitTestApplicationController.edit("125")(updateRequest)

      status(updateResult)(defaultAwaitTimeout) shouldBe Status.INTERNAL_SERVER_ERROR
      contentAsJson(updateResult)(defaultAwaitTimeout).as[JsValue] shouldBe Json.toJson("Bad response from upstream; Status: 400, Reason: Unable to edit book of ID: 125")
    }

    "return BadRequest if incorrect Json body" in {
      val emptyUpdateRequest: FakeRequest[JsValue] = buildPut("/api/edit/:id").withBody[JsValue](Json.obj())
      val emptyUpdateResult: Future[Result] = unitTestApplicationController.update("1")(emptyUpdateRequest)

      status(emptyUpdateResult)(defaultAwaitTimeout) shouldBe Status.BAD_REQUEST
    }
  }

  "ApplicationController .delete(id: String)" should {
    "find a book in the database by id and delete it" in {
      val deleteRequest: FakeRequest[AnyContentAsEmpty.type] = buildDelete("/api/:id")
      (mockApplicationService.delete(_: String))
        .expects("1")
        .returning(Future(Right(Accepted)))
        .once()
      val deleteResult: Future[Result] = unitTestApplicationController.delete("1")(deleteRequest)

      status(deleteResult)(defaultAwaitTimeout) shouldBe Status.ACCEPTED
    }
    "return a BadAPIResponse if the ID does not exist" in {
      val deleteWrongIdRequest: FakeRequest[AnyContentAsEmpty.type] = buildDelete("/api/:id")
      (mockApplicationService.delete(_: String))
        .expects("5")
        .returning(Future(Left(BadAPIResponse(400, "Unable to delete book of ID: 5"))))
        .once()
      val deleteWrongIdResult: Future[Result] = unitTestApplicationController.delete("5")(deleteWrongIdRequest)

      status(deleteWrongIdResult)(defaultAwaitTimeout) shouldBe Status.INTERNAL_SERVER_ERROR
      contentAsJson(deleteWrongIdResult)(defaultAwaitTimeout) shouldBe Json.toJson("Bad response from upstream; Status: 400, Reason: Unable to delete book of ID: 5")
    }
  }
}
