package play.api.mvc.hal

import play.api.libs.json.Json
import play.api.mvc._
import play.api.hal._

class HalWriteController(cc: ControllerComponents) extends AbstractController(cc) {

  def hal: Action[AnyContent] = Action {
    Ok(Hal.state(Json.obj("foo" -> "bar")))
  }

  def halOrJson: Action[AnyContent] = Action { implicit request =>
    render {
      case Accepts.Json() => Ok(Json.obj("foo" -> "bar"))
      case AcceptHal() =>
        Ok(Hal.state(Json.obj("foo" -> "bar")) ++ HalLink("self", "/orders"))
    }
  }
}
