package play.api

import play.api.libs.json._

/**
 * Scala model of the JSON Hypertext Application Language according to https://tools.ietf.org/html/draft-kelly-json-hal-06
 */
package object hal {

  object Defaults {
    val emptyJson = Json.parse("{}").as[JsObject]
  }

  case class HalResource(links: HalLinks, state: JsObject, embedded: Vector[(String, Vector[HalResource])] = Vector.empty) {
    def ++(other: HalResource): HalResource = {
      val d = state ++ other.state
      val l = links ++ other.links
      val e = embedded ++ other.embedded
      HalResource(l, d, e)
    }

    def include(other: HalResource) = ++(other)

    def ++(link: HalRelation): HalResource = {
      this.copy(links = links ++ link)
    }

    def include(link: HalRelation) = ++(link)
  }

//  case class HalLink(rel: String, href: String,
//      deprecation: Option[String] = None, name: Option[String] = None, profile: Option[String] = None,
//      title: Option[String] = None, hreflang: Option[String] = None, `type`: Option[String] = None,
//      linkAttr: JsObject = Defaults.emptyJson, templated: Boolean = false) {
//
//    def withLinkAttributes(obj: JsObject) = this.copy(linkAttr = obj)
//    def withDeprecation(url: String) = this.copy(deprecation = Some(url))
//    def withName(name: String) = this.copy(name = Some(name))
//    def withProfile(profile: String) = this.copy(profile = Some(profile))
//    def withTitle(title: String) = this.copy(title = Some(title))
//    def withHreflang(lang: String) = this.copy(hreflang = Some(lang))
//    def withType(mediaType: String) = this.copy(`type` = Some(mediaType))
//  }

  object HalLinks {
    def empty = HalLinks(Vector.empty)

//    implicit val halLinksWrites = Json.writes[HalLinks]


    implicit val halLinksWrites : Writes[HalLinks] = Writes {
      (halLinks : HalLinks) => {
        val _links = halLinks.links.foldLeft(JsObject(Seq())) { (_links, relation) => {
          val href : JsValue = relation match {
            case HalSingleRelation(rel, href) => Json.toJson(href)
            case HalMultipleRelation(rel, hrefs) => Json.toJson(hrefs)
          }
          _links.+ (relation.rel, href)
        }}

        JsObject(Seq(("_links", _links)))

//        val _links = halLinks.links.map { link =>
//          Json.toJson(link)
//        }
//

      }
    }
  }

  case class HalHref(href: String,
                     deprecation: Option[String] = None, name: Option[String] = None, profile: Option[String] = None,
                     title: Option[String] = None, hreflang: Option[String] = None, `type`: Option[String] = None,
                     linkAttr: JsObject = Defaults.emptyJson, templated: Boolean = false) {

    def withLinkAttributes(obj: JsObject) = this.copy(linkAttr = obj)
    def withDeprecation(url: String) = this.copy(deprecation = Some(url))
    def withName(name: String) = this.copy(name = Some(name))
    def withProfile(profile: String) = this.copy(profile = Some(profile))
    def withTitle(title: String) = this.copy(title = Some(title))
    def withHreflang(lang: String) = this.copy(hreflang = Some(lang))
    def withType(mediaType: String) = this.copy(`type` = Some(mediaType))
  }

  object HalHref{
    implicit val halHrefWrites : Writes[HalHref] = Writes {
      (href : HalHref) =>
        val result = JsObject(List("href" -> JsString(href.href)) ++
                optAttribute("deprecation", href.deprecation) ++
                optAttribute("name", href.name) ++
                optAttribute("profile", href.profile) ++
                optAttribute("title", href.title) ++
                optAttribute("type", href.`type`) ++
                optAttribute("hreflang", href.hreflang).toList) ++ href.linkAttr
        if (href.templated) result + ("templated" -> JsBoolean(true)) else result
    }
    def optAttribute(s: String, option: Option[String]) =
      option.map(value => (s, JsString(value)))


  }

  sealed trait HalRelation{
    val rel : String

  }

  case class HalSingleRelation(rel : String,  href : HalHref) extends HalRelation

  object HalSingleRelation {
    implicit val halSingleRelationFormat : Writes[HalSingleRelation] = Writes {
      (relation : HalSingleRelation) =>
        JsObject(List(relation.rel -> Json.toJson(relation.href)))
    }
  }

  case class HalMultipleRelation(rel : String, hrefs : Seq[HalHref]) extends HalRelation

  object HalMultipleRelation{
    implicit val halMultipleRelationFormat = Json.writes[HalMultipleRelation]
  }

  object HalRelation {

    implicit val halRelationWrite : Writes[HalRelation] = Writes {
      (relation : HalRelation) => relation match {
        case HalSingleRelation(rel, href) => JsObject(List(rel -> Json.toJson(href)))
        case HalMultipleRelation(rel, hrefs) => JsObject(List(rel -> Json.toJson(hrefs)))
      }
    }

    def apply(rel : String, href: String,
              deprecation: Option[String] = None, name: Option[String] = None, profile: Option[String] = None,
              title: Option[String] = None, hreflang: Option[String] = None, `type`: Option[String] = None,
              linkAttr: JsObject = Defaults.emptyJson, templated: Boolean = false) =
      HalSingleRelation(rel, HalHref(href, deprecation, name, profile, title, hreflang,`type`, linkAttr, templated))

//    def apply(rel : String, href : String) = HalSingleRelation(rel, HalHref(href))
    def apply(rel : String, href : HalHref) = HalSingleRelation(rel, href)
    def apply(rel : String, hrefs : Seq[HalHref]) = HalMultipleRelation(rel, hrefs)

  }

//  implicit

  case class HalLinks(links: Vector[HalRelation]) {
    def ++(other: HalLinks) = {
      HalLinks(links ++ other.links)
    }

    def include(other: HalLinks) = ++(other)

    def ++(link: HalRelation) = HalLinks(link +: this.links)

    def include(link: HalRelation) = ++(link)
  }

//  implicit val halLinkWrites = new Writes[HalLinks] {
//
//    def writes(hal: HalLinks): JsValue = {
//
//      val halLinks = hal.links.map { link =>
//        val href = hrefToJson(link)
//
//        link.rel -> href
//      }
//      Json.obj("_links" -> JsObject(halLinks))
//    }
//
//    def hrefToJson(link: HalLink): JsObject = {
//      val href = JsObject(List("href" -> JsString(link.href)) ++
//        optAttribute("deprecation", link.deprecation) ++
//        optAttribute("name", link.name) ++
//        optAttribute("profile", link.profile) ++
//        optAttribute("title", link.title) ++
//        optAttribute("type", link.`type`) ++
//        optAttribute("hreflang", link.hreflang).toList) ++ link.linkAttr
//      val maybeTemplated = if (link.templated) href + ("templated" -> JsBoolean(true)) else href
//      maybeTemplated
//    }
//
//    def optAttribute(s: String, option: Option[String]) =
//      option.map(value => (s, JsString(value)))
//  }

  implicit val halResourceWrites: Writes[HalResource] = new Writes[HalResource] {
    def writes(hal: HalResource): JsValue = {
      val embedded = toEmbeddedJson(hal)
      val resource = if (hal.links.links.isEmpty) hal.state
      else Json.toJson(hal.links).as[JsObject] ++ hal.state
      if (embedded.fields.isEmpty) resource
      else resource + ("_embedded" -> embedded)
    }

    def toEmbeddedJson(hal: HalResource): JsObject = {
      hal.embedded match {
        case Vector((k, Vector(elem))) => Json.obj((k, Json.toJson(elem)))
        case e if e.isEmpty => JsObject(Nil)
        case e => JsObject(e.map {
          case (link, resources) =>
            link -> Json.toJson(resources.map(r => Json.toJson(r)))
        })
      }
    }
  }
}
