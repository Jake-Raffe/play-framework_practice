package controllers

import baseSpec.BaseSpecWithApplication
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.Injecting

class ApplicationControllerSpec extends BaseSpecWithApplication with Injecting with GuiceOneAppPerSuite {

  val TestApplicationController = new ApplicationController(
    component,
    repository
  )

  "ApplicationController .index()" should {

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
