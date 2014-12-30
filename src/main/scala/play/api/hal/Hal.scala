package play.api.hal

import play.api.libs.json.{ JsValue, Json, Writes, JsObject }

object Hal {

  /**
   * A minimal HAL resource with only state and without links / embedded resources
   * @param content content representing the state
   * @tparam T something which can be converted to JSON
   */
  def state[T: Writes](content: T): HalResource = {
    HalResource(HalLinks.empty, Json.toJson(content).as[JsObject], Vector.empty)
  }

  /**
   * A HAL resource containing only links
   * @param links links to be contained in the resource
   */
  def links(links: HalLink*): HalResource = hal(JsObject(Nil), links.toVector)

  /**
   * A HAL resource containing only links
   * @param links links to be contained in the resource
   */
  def linksSeq(links: Seq[HalLink]): HalResource = hal(JsObject(Nil), links.toVector)

  /**
   * A HAL resource with at least one embedded resource
   * @param name type of the resources
   * @param embeds resources to be embedded
   */
  def embedded(name: String, embeds: HalResource*): HalResource = {
    HalResource(HalLinks.empty, JsObject(Nil), Vector(name -> embeds.toVector))
  }

  /**
   * A HAL resource with a embedded resource and its self link
   * @param link self link of the embedded resource
   * @param embed resource to be embedded
   */
  def embeddedLink(link: HalLink, embed: HalResource): HalResource = {
    links(link) ++ embedded(link.rel, embed ++ links(link.copy(rel = "self")))
  }

  /**
   * Construct a fully featured HAL resource by providing all of it components
   * @param content state of the resource
   * @param links links to resources
   * @param embedded embedded HAL resources
   */
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
