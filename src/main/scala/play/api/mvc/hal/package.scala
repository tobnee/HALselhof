package play.api.mvc

import play.api.hal._
import play.api.http.Writeable
import play.api.libs.json.Json

package object hal {

  val halMimeType = "application/hal+json"

  val AcceptHal: Accepting = Accepting(halMimeType)

  implicit def halWriter(implicit code: Codec): Writeable[HalResource] =
    Writeable(d => code.encode(Json.toJson(d).toString()), Some(halMimeType))
}
