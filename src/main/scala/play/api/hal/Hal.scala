package play.api.hal

import play.api.libs.json.{Json, Writes, JsObject}

object Hal {

  def state[T](content: T)(implicit cw: Writes[T]): HalResource = {
    HalResource(HalLinks.empty, Json.toJson(content)(cw).as[JsObject], Vector.empty)
  }

  def links(links: HalLink*) = hal(JsObject(Nil), links.toVector)

  def halSingle[T](content: T, embedded: (String, Vector[HalResource]), links: Vector[HalLink]
              )(implicit cw: Writes[T]): HalResource = {
    val (name, elems) = embedded
    hal(content, links, Vector(name -> elems))
  }

  def embedded(name: String, embeds: HalResource*): HalResource = {
    HalResource(HalLinks.empty, JsObject(Nil), Vector(name -> embeds.toVector))
  }

  def embeddedLink(link: HalLink, embed: HalResource): HalResource = {
    links(link) ++ embedded(link.name, embed ++ links(link.copy(name = "self")))
  }
  
  def hal[T](content: T, links: Vector[HalLink], embedded: Vector[(String, Vector[HalResource])] = Vector.empty
              )(implicit cw: Writes[T]): HalResource = {
    HalResource(
      HalLinks(links),
      Json.toJson(content)(cw).as[JsObject],
      embedded)
  }

  implicit class HalLinkToDocument(val link: HalLink) extends AnyVal {
    def asResource = Hal.links(link)
  }

  implicit class HalStateToDocument[T : Writes](val link: T) {
    def asResource = Hal.state(link)
  }

  implicit class HalResourceToJson(val hal: HalResource) extends AnyVal {
    def json = Json.toJson(hal)
  }

}
