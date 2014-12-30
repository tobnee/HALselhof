package play.api.hal

import play.api.libs.json.{ JsValue, Json, Writes, JsObject }

object Hal {

  def state[T: Writes](content: T): HalResource = {
    HalResource(HalLinks.empty, Json.toJson(content).as[JsObject], Vector.empty)
  }

  def links(links: HalLink*): HalResource = hal(JsObject(Nil), links.toVector)

  def linksSeq(links: Seq[HalLink]): HalResource = hal(JsObject(Nil), links.toVector)

  def halSingle[T: Writes](content: T, embedded: (String, Vector[HalResource]), links: Vector[HalLink]): HalResource = {
    val (name, elems) = embedded
    hal(content, links, Vector(name -> elems))
  }

  def embedded(name: String, embeds: HalResource*): HalResource = {
    HalResource(HalLinks.empty, JsObject(Nil), Vector(name -> embeds.toVector))
  }

  def embeddedLink(link: HalLink, embed: HalResource): HalResource = {
    links(link) ++ embedded(link.rel, embed ++ links(link.copy(rel = "self")))
  }

  def hal[T: Writes](content: T, links: Vector[HalLink], embedded: Vector[(String, Vector[HalResource])] = Vector.empty): HalResource = {
    HalResource(
      HalLinks(links),
      Json.toJson(content).as[JsObject],
      embedded)
  }

  implicit class HalLinkToResource(val link: HalLink) extends AnyVal {
    def asResource = Hal.links(link)
  }

  implicit class JsonToResource(val jsValue: JsValue) extends AnyVal {
    def asResource = Hal.state(jsValue)
  }

  implicit class HalStateToResource[T: Writes](val link: T) {
    def asResource = Hal.state(link)
  }

  implicit class HalResourceToJson(val hal: HalResource) extends AnyVal {
    def json = Json.toJson(hal)
  }

}
