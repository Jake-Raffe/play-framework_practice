package services

import baseSpec.{BaseSpec, BaseSpecWithApplication}
import models.APIError.BadAPIResponse
import models.{APIError, DataModel, UpdateField}
import org.mongodb.scala.result
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.{Eventually, ScalaFutures}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContentAsEmpty, Request, Result}
import play.api.mvc.Results.{Accepted, Created, Ok}
import play.api.http.Status
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.FakeRequest
import play.api.test.Helpers.{DELETE, GET, POST, PUT, await, contentAsJson, defaultAwaitTimeout, status}
import repositories.{DataRepository, DataRepositoryTrait}
import services.{ApplicationService, LibraryService}

import scala.concurrent.Future
import scala.util.Left

class ApplicationServiceSpec extends BaseSpec with MockFactory with ScalaFutures with Eventually {

  import scala.concurrent.ExecutionContext.Implicits.global

  val mockDataRepository = mock[DataRepositoryTrait]
  val testApplicationService = new ApplicationService(mockDataRepository)

  private val mockDataModel: DataModel = DataModel(
    "1",
    "Mock Book",
    "This is a data model book for testing",
    100
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
  private val emptyData = new DataModel("empty", "", "", 0)

  private def buildPost(url: String): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(POST, url).withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]
  private def buildGet(url: String): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, url).withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]
  private def buildPut(url: String): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(PUT, url).withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]
  private def buildDelete(url: String): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(DELETE, url).withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

  "ApplicationService .create(book: DataModel)" should {
    "validate the request body is of a DataModel format and return the book that has been added to the database" in {
      val request: FakeRequest[JsValue] = buildPost("/api").withBody[JsValue](Json.toJson(mockDataModel))
      (mockDataRepository.create(_: DataModel))
        .expects(mockDataModel)
        .returning(Future(Right(mockDataModel)))
        .once()

      whenReady(testApplicationService.create(request)) { result =>
        result shouldBe Right(mockDataModel)
      }
    }

    "return a BadAPIResponse wrapped in a Left and a Future if the request body is of the wrong format" in {
      val badRequest: FakeRequest[JsValue] = buildPost("/api").withBody[JsValue](Json.obj())
      val badCreatedResult: Future[Either[APIError, DataModel]] = testApplicationService.create(badRequest)

      whenReady(testApplicationService.create(badRequest)) { result =>
        result shouldBe Left(APIError.BadAPIResponse(415, "Unable to validate request body format"))
      }
    }
  }

  // read by name fails, if done individually both success tests fail
  "ApplicationService .read(findBy: String, identifier: String)" should {
    "find a book in the database by id and returned it as a Json object in an OK body" in {
//      val readRequest: FakeRequest[AnyContentAsEmpty.type] = buildGet("/api/id/:id")
      (mockDataRepository.read(_: String, _: String))
        .expects("ID", "1")
        .returning(Future(mockDataModel))
        .once()

      whenReady(testApplicationService.read("ID", "1")) { result =>
        result shouldBe Right(Ok(Json.toJson(mockDataModel)))
      }
    }
    "find a book in the database by name and returned it as a Json object in an OK body" in {
//      val readRequest: FakeRequest[AnyContentAsEmpty.type] = buildGet("/api/name/:name")
      (mockDataRepository.read(_: String, _: String))
        .expects("name", "Mock Book")
        .returning(Future(mockDataModel))
        .once()

      whenReady(testApplicationService.read("name", "Mock Book")) { result =>
        result shouldBe Right(Ok(Json.toJson(mockDataModel)))
      }
    }
    "return a BadAPIResponse if a book of the ID is not found" in {
      //      val readRequest: FakeRequest[AnyContentAsEmpty.type] = buildGet("/api/id/:id")
      (mockDataRepository.read(_: String, _: String))
        .expects("ID", "22")
        .returning(Future(emptyData))
        .once()

      whenReady(testApplicationService.read("ID", "22")) { result =>
        result shouldBe Left(BadAPIResponse(404, s"Unable to find book of ID: 22"))
      }
    }
    "return a BadAPIResponse if a book of the name is not found" in {
      //      val readRequest: FakeRequest[AnyContentAsEmpty.type] = buildGet("/api/name/:name")
      (mockDataRepository.read(_: String, _: String))
        .expects("name", "Wrong name book")
        .returning(Future(emptyData))
        .once()

      whenReady(testApplicationService.read("name", "Wrong name book")) { result =>
        result shouldBe Left(BadAPIResponse(404, "Unable to find book of name: Wrong name book"))
      }
    }
  }

  "ApplicationService .update(id: String, newBook: DataModel)" should {
    "find a book in the database by it's ID and replace it with the new book, returning the updated book" in {
      (mockDataRepository.update(_: String, _: DataModel))
        .expects("1", updatedMockDataModel)
        .returning(Future(Right(updatedMockDataModel)))
        .once()

      whenReady(testApplicationService.update("1", updatedMockDataModel)) { result =>
        result shouldBe Right(Accepted(Json.toJson(updatedMockDataModel)))
      }
    }

    "return BadAPIResponse message if wrong ID" in {
      val updateRequest: FakeRequest[JsValue] = buildPut("/api/:id").withBody[JsValue](Json.toJson(updatedMockDataModel))
      (mockDataRepository.update(_: String, _: DataModel))
        .expects("125", updatedMockDataModel)
        .returning(Future(Left(APIError.BadAPIResponse(400, "Unable to update book of ID: 125"))))
        .once()

      whenReady(testApplicationService.update("125", updatedMockDataModel)) { result =>
        result shouldBe Left(APIError.BadAPIResponse(400, "Unable to update book of ID: 125"))
      }
    }
  }

  "ApplicationController .edit(id: String)" should {

    "find a book in the database by it's ID and replace a field with an edit before returning the updated book" in {
      (mockDataRepository.edit(_: String, _: String, _: String))
        .expects("1", updateField.fieldName.toString, updateField.edit.toString)
        .returning(Future(Some(editedMockDataModel)))
        .once()

      whenReady(testApplicationService.edit("1", updateField)) { result =>
        result shouldBe Right(Accepted(Json.toJson(editedMockDataModel)))
      }
    }

    "return BadAPIResponse message if wrong ID" in {
      (mockDataRepository.edit(_: String, _: String, _: String))
        .expects("125", updateField.fieldName.toString, updateField.edit.toString)
        .returning(Future(None))
        .once()

      whenReady(testApplicationService.edit("125", updateField)) { result =>
        result shouldBe Left(APIError.BadAPIResponse(400, "Unable to edit book of ID: 125"))
      }
    }
  }

  "ApplicationController .delete(id: String)" should {
    "find a book in the database by id and delete it" in {
      (mockDataRepository.delete(_: String))
        .expects("1")
        .returning(Future(1L))
        .once()

      whenReady(testApplicationService.delete("1")) { result =>
        result shouldBe Right(Accepted)
      }
    }
    "return a BadAPIResponse if the ID does not exist" in {
      (mockDataRepository.delete(_: String))
        .expects("5")
        .returning(Future(0L))
        .once()

      whenReady(testApplicationService.delete("5")) { result =>
        result shouldBe Left(BadAPIResponse(400, "Unable to delete book of ID: 5"))
      }
    }
  }
}


