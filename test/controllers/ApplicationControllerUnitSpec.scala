package controllers

import baseSpec.BaseSpecWithApplication
import models.{APIError, DataModel, UpdateField}
import org.scalamock.scalatest.MockFactory
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Request, Result}
import play.api.mvc.Results.Created
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsJson, defaultAwaitTimeout, status}
import services.{ApplicationService, LibraryService}

import scala.concurrent.Future

class ApplicationControllerUnitSpec extends BaseSpecWithApplication with MockFactory{

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
    }

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

}
