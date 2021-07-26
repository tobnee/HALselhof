package play.api.mvc.hal

import org.scalatestplus.play.PlaySpec
import play.api.mvc.Result
import play.api.test.Helpers.{ contentAsJson, contentType, stubControllerComponents }
import play.api.test.{ DefaultAwaitTimeout, FakeRequest }

import scala.concurrent.Future

class ControllerTest extends PlaySpec with DefaultAwaitTimeout {

  val controller = new HalWriteController(stubControllerComponents())

  "A HAL Resource" should {

    "be writeable" in {
      val result: Future[Result] = controller.hal().apply(FakeRequest())
      contentType(result) mustBe Some("application/hal+json")
      (contentAsJson(result) \ "foo").as[String] mustBe "bar"
    }

    "be retrieved as JSON" in {
      val result: Future[Result] = controller.halOrJson.apply(FakeRequest().withHeaders("Accept" -> "application/json"))
      contentType(result) mustBe Some("application/json")
    }

    "be retrieved as HAL" in {
      val result: Future[Result] =
        controller.halOrJson.apply(FakeRequest().withHeaders("Accept" -> "application/hal+json"))
      contentType(result) mustBe Some("application/hal+json")
    }
  }
}
