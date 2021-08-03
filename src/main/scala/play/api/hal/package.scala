package play.api

import play.api.libs.json._

/** Scala model of the JSON Hypertext Application Language according to https://tools.ietf.org/html/draft-kelly-json-hal-06
  */
package object hal {

  object Defaults {
    val emptyJson: JsObject = Json.parse("{}").as[JsObject]
  }

  case class HalResource(
      links: HalLinks,
      state: JsObject,
      embedded: Vector[(String, Vector[HalResource])] = Vector.empty
  ) {

    def ++(other: HalResource): HalResource = {
      val d = state ++ other.state
      val l = links ++ other.links
      val e = embedded ++ other.embedded
      HalResource(l, d, e)
    }

    def include(other: HalResource): HalResource = ++(other)

    def ++(link: HalRelation): HalResource =
      this.copy(links = links ++ link)

    def include(link: HalRelation): HalResource = ++(link)
  }

  object HalLinks {
    def empty: HalLinks = HalLinks(Vector.empty)

    implicit val halLinksWrites: Writes[HalLinks] = Writes { (halLinks: HalLinks) =>
      {
        val _links = halLinks.links.foldLeft(JsObject(Seq())) { (_links, relation) =>
          {
            val href: JsValue = relation match {
              case HalSingleRelation(rel, href)    => Json.toJson(href)
              case HalMultipleRelation(rel, hrefs) => Json.toJson(hrefs)
            }
            _links.+((relation.rel, href))
          }
        }
        JsObject(Seq(("_links", _links)))
      }
    }
  }

  case class HalHref(
      href: String,
      deprecation: Option[String] = None,
      name: Option[String] = None,
      profile: Option[String] = None,
      title: Option[String] = None,
      hreflang: Option[String] = None,
      `type`: Option[String] = None,
      linkAttr: JsObject = Defaults.emptyJson,
      templated: Boolean = false
  ) {

    def withLinkAttributes(obj: JsObject): HalHref = this.copy(linkAttr = obj)
    def withDeprecation(url: String): HalHref = this.copy(deprecation = Some(url))
    def withName(name: String): HalHref = this.copy(name = Some(name))
    def withProfile(profile: String): HalHref = this.copy(profile = Some(profile))
    def withTitle(title: String): HalHref = this.copy(title = Some(title))
    def withHreflang(lang: String): HalHref = this.copy(hreflang = Some(lang))
    def withType(mediaType: String): HalHref = this.copy(`type` = Some(mediaType))
  }

  object HalHref {
    implicit val halHrefWrites: Writes[HalHref] = Writes { (href: HalHref) =>
      val result = JsObject(
        List("href" -> JsString(href.href)) ++
          optAttribute("deprecation", href.deprecation) ++
          optAttribute("name", href.name) ++
          optAttribute("profile", href.profile) ++
          optAttribute("title", href.title) ++
          optAttribute("type", href.`type`) ++
          optAttribute("hreflang", href.hreflang).toList
      ) ++ href.linkAttr
      if (href.templated) result + ("templated" -> JsBoolean(true)) else result
    }
    def optAttribute(s: String, option: Option[String]): Option[(String, JsString)] =
      option.map(value => (s, JsString(value)))
  }

  sealed trait HalRelation {
    val rel: String
  }

  case class HalSingleRelation(rel: String, href: HalHref) extends HalRelation

  object HalSingleRelation {
    implicit val halSingleRelationFormat: Writes[HalSingleRelation] = Writes { (relation: HalSingleRelation) =>
      JsObject(List(relation.rel -> Json.toJson(relation.href)))
    }
  }

  case class HalMultipleRelation(rel: String, hrefs: Seq[HalHref]) extends HalRelation

  object HalMultipleRelation {
    implicit val halMultipleRelationFormat = Json.writes[HalMultipleRelation]
  }

  object HalRelation {

    implicit val halRelationWrite: Writes[HalRelation] = Writes { (relation: HalRelation) =>
      relation match {
        case HalSingleRelation(rel, href)    => JsObject(List(rel -> Json.toJson(href)))
        case HalMultipleRelation(rel, hrefs) => JsObject(List(rel -> Json.toJson(hrefs)))
      }
    }

    def apply(
        rel: String,
        href: String,
        deprecation: Option[String] = None,
        name: Option[String] = None,
        profile: Option[String] = None,
        title: Option[String] = None,
        hreflang: Option[String] = None,
        `type`: Option[String] = None,
        linkAttr: JsObject = Defaults.emptyJson,
        templated: Boolean = false
    ): HalSingleRelation =
      HalSingleRelation(rel, HalHref(href, deprecation, name, profile, title, hreflang, `type`, linkAttr, templated))

    def apply(rel: String, href: HalHref): HalSingleRelation = HalSingleRelation(rel, href)
    def apply(rel: String, hrefs: Seq[HalHref]): HalMultipleRelation = HalMultipleRelation(rel, hrefs)
  }

  case class HalLinks(links: Vector[HalRelation]) {

    def ++(other: HalLinks): HalLinks =
      HalLinks(links ++ other.links)

    def include(other: HalLinks): HalLinks = ++(other)

    def ++(link: HalRelation): HalLinks = HalLinks(link +: this.links)

    def include(link: HalRelation): HalLinks = ++(link)
  }

  implicit val halResourceWrites: Writes[HalResource] =
    new Writes[HalResource] {

      def writes(hal: HalResource): JsValue = {
        val embedded = toEmbeddedJson(hal)
        val resource =
          if (hal.links.links.isEmpty) hal.state
          else Json.toJson(hal.links).as[JsObject] ++ hal.state
        if (embedded.fields.isEmpty) resource
        else resource + ("_embedded" -> embedded)
      }

      def toEmbeddedJson(hal: HalResource): JsObject =
        hal.embedded match {
          case Vector((k, Vector(elem))) => Json.obj((k, Json.toJson(elem)))
          case e if e.isEmpty            => JsObject(Nil)
          case e =>
            JsObject(e.map { case (link, resources) =>
              link -> Json.toJson(resources.map(r => Json.toJson(r)))
            })
        }
    }
}
