import org.scalatest.{Matchers, FunSuite}
import play.api.libs.json.Json
import play.api.hal._

class TestBase extends FunSuite  with Matchers {

  case class TestJson(total: Int, currency: String, status: String)
  implicit val testWrites = Json.writes[TestJson]

  test("A mininmal HAL document is a normal JSON") {
    val json = TestJson(20, "EUR", "shipped")
    Json.toJson(Hal.state(json)) should equal(Json.toJson(json))
  }

  test("A HAL document may contain only links") {
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

  test("A HAL document may contain links and state") {
    val json = TestJson(20, "EUR", "shipped")
    Json.toJson(Hal.state(json))
    Json.toJson(
      Hal.state(json) ++
      Hal.links(
        HalLink("self", "/orders"),
        HalLink("next", "/orders?page=2"),
        HalLink("find", "/orders{?id}", templated = true))) should equal(
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

  test("a HAL document may embed links") {
    val json = TestJson(20, "EUR", "shipped")
    Json.toJson(Hal.state(json))
    Json.toJson(
      Hal.links(HalLink("self", "/blog-post"), HalLink("author", "/people/alan-watts")) ++
        Hal.embedded("author", Hal.links(HalLink("self", "/people/alan-watts")) ++ Hal.state(
            Json.obj("name" -> "Alan Watts",
                     "born" -> "January 6, 1915",
                     "died" ->"November 16, 1973")))
        ) should equal(
      Json.parse("""{
                 "_links": {
                   "self": { "href": "/blog-post" },
                   "author": { "href": "/people/alan-watts" }
                 },
                 "_embedded": {
                   "author": [{
                     "_links": { "self": { "href": "/people/alan-watts" } },
                     "name": "Alan Watts",
                     "born": "January 6, 1915",
                     "died": "November 16, 1973"
                   }]
                 }
                  }""".stripMargin))
  }


}
