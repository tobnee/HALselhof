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
  def links(links: HalRelation*): HalResource = hal(JsObject(Nil), links.toVector)

  /**
   * A HAL resource containing only links
   * @param links links to be contained in the resource
   */
  def linksSeq(links: Seq[HalRelation]): HalResource = hal(JsObject(Nil), links.toVector)

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
  def embeddedLink(link: HalRelation, embed: HalResource): HalResource = {
    link match {
      case sr : HalSingleRelation => links(sr) ++ embedded(sr.rel, embed ++ links(sr.copy(rel = "self")))
      case mr : HalMultipleRelation=> links(mr ) ++ embedded(mr .rel, embed ++ links(mr .copy(rel = "self")))
    }

  }

  /**
   * Construct a fully featured HAL resource by providing all of it components
   * @param content state of the resource
   * @param links links to resources
   * @param embedded embedded HAL resources
   */
  def hal[T: Writes](content: T, links: Vector[HalRelation], embedded: Vector[(String, Vector[HalResource])] = Vector.empty): HalResource = {
    HalResource(
      HalLinks(links),
      Json.toJson(content).as[JsObject],
      embedded)
  }


//  implicit class HalLinkToResource(val link: HalLink) extends AnyVal {
//    def asResource = Hal.links(link)
//  }

  implicit class HalRelationToResource(val relationLink : HalRelation) extends AnyVal {
    def asResource = Hal.links(relationLink)
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
