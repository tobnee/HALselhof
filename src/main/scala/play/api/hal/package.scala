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

    def ++(link: HalLink): HalResource =
      this.copy(links = links ++ link)

    def include(link: HalLink): HalResource = ++(link)
  }

  case class HalLink(
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
  ) {

    def withLinkAttributes(obj: JsObject): HalLink = this.copy(linkAttr = obj)
    def withDeprecation(url: String): HalLink = this.copy(deprecation = Some(url))
    def withName(name: String): HalLink = this.copy(name = Some(name))
    def withProfile(profile: String): HalLink = this.copy(profile = Some(profile))
    def withTitle(title: String): HalLink = this.copy(title = Some(title))
    def withHreflang(lang: String): HalLink = this.copy(hreflang = Some(lang))
    def withType(mediaType: String): HalLink = this.copy(`type` = Some(mediaType))
  }

  object HalLinks {
    def empty: HalLinks = HalLinks(Vector.empty)
  }

  case class HalLinks(links: Vector[HalLink]) {

    def ++(other: HalLinks): HalLinks =
      HalLinks(links ++ other.links)

    def include(other: HalLinks): HalLinks = ++(other)

    def ++(link: HalLink): HalLinks = HalLinks(link +: this.links)

    def include(link: HalLink): HalLinks = ++(link)
  }

  implicit val halLinkWrites: Writes[HalLinks] = new Writes[HalLinks] {

    def writes(hal: HalLinks): JsValue = {

      val halLinks = hal.links.map { link =>
        val href = linkToJson(link)

        val links =
          if (link.templated) href + ("templated" -> JsBoolean(true)) else href
        link.rel -> links
      }
      Json.obj("_links" -> JsObject(halLinks))
    }

    def linkToJson(link: HalLink): JsObject =
      JsObject(
        List("href" -> JsString(link.href)) ++
          optAttribute("deprecation", link.deprecation) ++
          optAttribute("name", link.name) ++
          optAttribute("profile", link.profile) ++
          optAttribute("title", link.title) ++
          optAttribute("type", link.`type`) ++
          optAttribute("hreflang", link.hreflang).toList
      ) ++ link.linkAttr

    def optAttribute(s: String, option: Option[String]): Option[(String, JsString)] =
      option.map(value => (s, JsString(value)))
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
