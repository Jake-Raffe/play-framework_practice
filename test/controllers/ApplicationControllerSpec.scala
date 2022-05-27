package controllers

import baseSpec.BaseSpecWithApplication
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.Injecting
import play.api.test.FakeRequest
import play.api.http.Status
import play.api.test.Helpers.status

class ApplicationControllerSpec extends BaseSpecWithApplication with Injecting with GuiceOneAppPerSuite {

  val TestApplicationController = new ApplicationController(
    component,
    repository
  )

  "ApplicationController .index()" should {
    val result = TestApplicationController.index()(FakeRequest())

    "return TODO" in {
      status(result) shouldBe Status.OK
    }
  }

  "ApplicationController .create()" should {

  }

  "ApplicationController .read(id: String)" should {

  }

  "ApplicationController .update(id: String)" should {

  }

  "ApplicationController .delete(id: String)" should {

  }
}
