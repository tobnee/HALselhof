package play.api

import play.api.libs.json._

package object hal {

  case class HalResource(links: HalLinks, state: JsObject, embedded: Vector[(String, Vector[HalResource])] = Vector.empty) {
    def ++(other: HalResource): HalResource = {
      val d = state ++ other.state
      val l = links ++ other.links
      val e = embedded ++ other.embedded
      HalResource(l, d, e)
    }
  }

  case class HalLink(name: String, href: String, templated: Boolean = false)

  object HalLinks {
    def empty = HalLinks(Vector.empty)
  }

  case class HalLinks(links: Vector[HalLink]) {
    def ++(other: HalLinks) = {
      HalLinks(links ++ other.links)
    }
  }

  implicit val halLinkWrites = new Writes[HalLinks] {
    def writes(hal: HalLinks): JsValue = {
      val halLinks = hal.links.map { link =>
        val href = Json.obj("href" -> JsString(link.href))
        val links = if (link.templated) href + ("templated" -> JsBoolean(true)) else href
        link.name -> links
      }
      Json.obj("_links" -> JsObject(halLinks))
    }
  }

  implicit val halResourceWrites: Writes[HalResource] = new Writes[HalResource] {
    def writes(hal: HalResource): JsValue = {

      val embedded = hal.embedded match {
        case Vector((k, Vector(elem))) => Json.obj((k, Json.toJson(elem)))
        case e if e.isEmpty => JsObject(Nil)
        case e => Json.toJson(e.map {
          case (link, resources) =>
            Json.obj(link -> Json.toJson(resources.map(r => Json.toJson(r))))
        })
      }

      val resource = if (hal.links.links.isEmpty) hal.state
      else Json.toJson(hal.links).as[JsObject] ++ hal.state
      if (hal.embedded.isEmpty) resource
      else resource + ("_embedded" -> embedded)
    }
  }
}
