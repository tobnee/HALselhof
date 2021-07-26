package play.api.mvc.hal

import org.scalatest.{ FunSuite, Matchers }
import play.api.http.{ HeaderNames, Status }
import play.api.libs.json.Json
import play.api.mvc.{ Controller, Result }
import play.api.test.{ DefaultAwaitTimeout, FakeRequest, ResultExtractors }

import scala.concurrent.Future

class ControllerTest
    extends FunSuite
    with Matchers
    with ResultExtractors
    with HeaderNames
    with Status
    with DefaultAwaitTimeout {

  class TestController() extends Controller with HalWriteController

  test("A HAL Resource should be writeable") {
    val controller = new TestController()
    val result: Future[Result] = controller.hal().apply(FakeRequest())
    val bodyText: String = contentAsString(result)
    contentType(result) should equal(Some("application/hal+json"))
    (Json.parse(bodyText) \ "foo").as[String] should equal("bar")
  }

  test("A Resource can be retrived as JSON") {
    val controller = new TestController()
    val result: Future[Result] = controller.halOrJson.apply(FakeRequest().withHeaders("Accept" -> "application/json"))
    contentType(result) should equal(Some("application/json"))
  }

  test("A Resource can be retrived as HAL") {
    val controller = new TestController()
    val result: Future[Result] =
      controller.halOrJson.apply(FakeRequest().withHeaders("Accept" -> "application/hal+json"))
    contentType(result) should equal(Some("application/hal+json"))
  }

}
