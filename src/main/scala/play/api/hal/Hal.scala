package play.api.hal

import play.api.libs.json.{Json, Writes, JsObject}

object Hal {

  def state[T](content: T)(implicit cw: Writes[T]): HalDocument = {
    HalDocument(HalLinks.empty, Json.toJson(content)(cw).as[JsObject], Vector.empty)
  }

  def links(links: HalLink*) = hal(JsObject(Nil), links.toVector)

  def halSingle[T](content: T, embedded: (String, Vector[HalDocument]), links: Vector[HalLink]
              )(implicit cw: Writes[T]): HalDocument = {
    val (name, elems) = embedded
    hal(content, links, Vector(name -> elems))
  }

  def embedded(name: String, embeds: HalDocument*): HalDocument = {
    HalDocument(HalLinks.empty, JsObject(Nil), Vector(name -> embeds.toVector))
  }

  def embeddedLink(link: HalLink, embed: HalDocument): HalDocument = {
    links(link) ++ embedded(link.name, embed ++ links(link.copy(name = "self")))
  }
  
  def hal[T](content: T, links: Vector[HalLink], embedded: Vector[(String, Vector[HalDocument])] = Vector.empty
              )(implicit cw: Writes[T]): HalDocument = {
    HalDocument(
      HalLinks(links),
      Json.toJson(content)(cw).as[JsObject],
      embedded)
  }

}
