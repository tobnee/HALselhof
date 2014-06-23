import org.scalatest.{Matchers, FunSuite}
import play.api.libs.json.Json
import play.api.hal._
import play.api.hal.Hal._

class TestHalConstruction extends FunSuite  with Matchers {

  case class TestData(total: Int, currency: String, status: String)
  implicit val testWrites = Json.writes[TestData]

  test("A mininmal HAL resource is a JSON object") {
    val data = TestData(20, "EUR", "shipped")
    data.asResource.json should equal(Json.toJson(data))
  }

  test("A HAL resource may contain only links") {
    Json.toJson(
      Hal.links(
        HalLink("self", "/orders"),
        HalLink("next", "/orders?page=2"),
        HalLink("find", "/orders{?id}", templated = true))) should equal(
          Json.parse("""{
                       "_links": {
                       "self": { "href": "/orders" },
                       "next": { "href": "/orders?page=2" },
                       "find": { "href": "/orders{?id}", "templated": true }
                        }
                        }""".stripMargin))
  }

  test("A HAL resouce may contain links and state") {
    val json = TestData(20, "EUR", "shipped")
    (Hal.state(json) ++
      Hal.links(
        HalLink("self", "/orders"),
        HalLink("next", "/orders?page=2"),
        HalLink("find", "/orders{?id}", templated = true))).json should equal(
      Json.parse("""{
                       "_links": {
                       "self": { "href": "/orders" },
                       "next": { "href": "/orders?page=2" },
                       "find": { "href": "/orders{?id}", "templated": true }
                        },
                         "total" : 20,
                         "currency" : "EUR",
                         "status": "shipped"
                        }""".stripMargin))
  }

  test("a HAL resource may embed links") {
    val json = TestData(20, "EUR", "shipped")
    Json.toJson(Hal.state(json))
    val selfLink = HalLink("self", "/blog-post").asResource
    val authorLink = HalLink("author", "/people/alan-watts")
    val embeddedAuthorState = Json.obj(
      "name" -> "Alan Watts",
      "born" -> "January 6, 1915",
      "died" -> "November 16, 1973").asResource

    (selfLink ++ Hal.embeddedLink(authorLink, embeddedAuthorState)).json should equal(
      Json.parse("""{
                 "_links": {
                   "self": { "href": "/blog-post" },
                   "author": { "href": "/people/alan-watts" }
                 },
                 "_embedded": {
                   "author": {
                     "_links": { "self": { "href": "/people/alan-watts" } },
                     "name": "Alan Watts",
                     "born": "January 6, 1915",
                     "died": "November 16, 1973"
                   }
                 }
                  }""".stripMargin))
  }


}
