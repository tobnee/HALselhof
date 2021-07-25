package play.api.mvc.hal

import play.api.libs.json.Json
import play.api.mvc._
import play.api.hal._
import play.api.mvc.hal._

trait HalWriteController {
  this: Controller =>

  def hal = Action {
    Ok(Hal.state(Json.obj("foo" -> "bar")))
  }

  def halOrJson = Action { implicit request =>
    render {
      case Accepts.Json() => Ok(Json.obj("foo" -> "bar"))
      case AcceptHal() =>
        Ok(Hal.state(Json.obj("foo" -> "bar")) ++ HalLink("self", "/orders"))
    }
  }

}
