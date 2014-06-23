package play.api

import play.api.libs.json._

package object hal {

  case class  HalDocument(links: HalLinks, document: JsObject, embedded: Vector[(String, Vector[HalDocument])] = Vector.empty) {
    def ++ (other: HalDocument): HalDocument = {
      val d = document ++ other.document
      val l = links ++ other.links
      val e = embedded ++ other.embedded
      HalDocument(l, d, e)
    }
  }

  case class HalLink(name: String, href: String, templated: Boolean = false)

  object HalLinks {
    def empty = HalLinks(Vector.empty)
  }

  case class HalLinks(links: Vector[HalLink]) {
    def ++ (other: HalLinks) = {
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

  implicit val halDocumentWrites: Writes[HalDocument] = new Writes[HalDocument] {
    def writes(hal: HalDocument): JsValue = {
      val embedded = hal.embedded.map {
        case (link, resources) =>
          link -> Json.toJson(resources.map(r => Json.toJson(r)))
      }
      val document = if(hal.links.links.isEmpty) hal.document
                     else Json.toJson(hal.links).as[JsObject] ++ hal.document
      if (embedded.isEmpty) document
      else document + ("_embedded" -> JsObject(embedded))
    }
  }
}
