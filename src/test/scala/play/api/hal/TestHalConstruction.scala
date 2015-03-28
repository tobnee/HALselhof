package play.api.hal

import org.scalatest.{ FunSuite, Matchers }
import play.api.hal.Hal._
import play.api.hal.HalRelation
import play.api.libs.json.Json

class TestHalConstruction extends FunSuite with Matchers {

  case class TestData(total: Int, currency: String, status: String)
  implicit val testWrites = Json.writes[TestData]

  test("A mininmal HAL resource is a JSON object") {
    val data = TestData(20, "EUR", "shipped")
    data.asResource.json should equal(Json.toJson(data))
  }

  test("A Hal Href is a JSON object") {
    val data = HalHref("/orders")
    data.asResource.json should equal(Json.parse(
      """{
          "href": "/orders"
          }
      """.stripMargin))
  }

  test("A Hal Relation may be a single link") {
    val data : HalSingleRelation = HalRelation("order", HalHref("/order"))
    data.asResource.json should equal(Json.parse(
      """{
         "_links": {
         "order" : { "href": "/order"}
          }
          }""".stripMargin))
  }

  test("A Hal Link may share a relation with another link") {
    (Hal.links(
      HalRelation("orders", List(HalHref("/orders/1"), HalHref("/orders/2"))),
      HalRelation("find", "/search{?id}", templated = true))).json should equal(Json.parse(
      """{
         "_links": {
         "orders" : [{ "href": "/orders/1"}, { "href": "/orders/2"}],
         "find":{"href":"/search{?id}","templated":true}
          }
          }""".stripMargin))
  }

//  test("A Hal Link may share a relation with another link 2") {
//    (Hal.links(
//      HalRelation("foo", "/bar/1"),
//      HalRelation("foo", "/bar/2")
//      )).json should equal(Json.parse(
//      """{
//         "_links": {
//         "foo" : [{ "href": "/bar/1"}, { "href": "/bar/2"}]
//          }
//          }""".stripMargin))
//  }


  test("A HAL resource may contain only links") {
    (Hal.links(
      HalRelation("self", "/orders"),
      HalRelation("next", "/orders?page=2"),
      HalRelation("find", "/orders{?id}", templated = true))).json should equal(
        Json.parse("""{
                       "_links": {
                       "self": { "href": "/orders" },
                       "next": { "href": "/orders?page=2" },
                       "find": { "href": "/orders{?id}", "templated": true }
                        }
                        }""".stripMargin))
  }

  test("A HAL resouce may contain links and state") {
    val data = TestData(20, "EUR", "shipped")
    (data.asResource ++
      HalRelation("self", "/orders") ++
      HalRelation("next", "/orders?page=2") ++
      HalRelation("find", "/orders{?id}", templated = true)).json should equal(
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
    val selfLink = HalRelation("self", "/blog-post").asResource
    val authorLink = HalRelation("author", "/people/alan-watts")
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

  test("a HAL resource may embed multible resources") {
    val baseResource =
      Json.obj("currentlyProcessing" -> 14, "shippedToday" -> 20).asResource ++
        HalRelation("self", "/orders") ++
        HalRelation("next", "/orders?page=2") ++
        HalRelation("find", "/orders{?id}", templated = true)

    val resource1 =
      TestData(30, "USD", "shipped").asResource ++
        HalRelation("self", "/orders/123") ++
        HalRelation("basket", "/baskets/98712") ++
        HalRelation("customer", "/customers/7809")

    val resource2 =
      TestData(20, "USD", "processing").asResource ++
        HalRelation("self", "/orders/124") ++
        HalRelation("basket", "/baskets/97213") ++
        HalRelation("customer", "/customers/12369")

    val res = baseResource ++ Hal.embedded("orders", resource1, resource2)

    res.json should equal(Json.parse("""{
      "_links": {
        "self": { "href": "/orders" },
        "next": { "href": "/orders?page=2" },
        "find": { "href": "/orders{?id}", "templated": true }
      },
      "_embedded": {
        "orders": [{
            "_links": {
              "self": { "href": "/orders/123" },
              "basket": { "href": "/baskets/98712" },
              "customer": { "href": "/customers/7809" }
            },
            "total": 30,
            "currency": "USD",
            "status": "shipped"
          },{
            "_links": {
              "self": { "href": "/orders/124" },
              "basket": { "href": "/baskets/97213" },
              "customer": { "href": "/customers/12369" }
            },
            "total": 20,
            "currency": "USD",
            "status": "processing"
        }]
      },
      "currentlyProcessing": 14,
      "shippedToday": 20
      }""".stripMargin))
  }

  test("provide alternative names for composition") {
    val data = TestData(20, "EUR", "shipped")
    data.asResource ++
      HalRelation("self", "/orders") ++
      HalRelation("next", "/orders?page=2") ++
      HalRelation("find", "/orders{?id}", templated = true) should equal(

        data.asResource include
          HalRelation("self", "/orders") include
          HalRelation("next", "/orders?page=2") include
          HalRelation("find", "/orders{?id}", templated = true)
      )
  }

  test("provide support for optional link attributes") {
    Hal.links(
      HalRelation("self", HalHref("/orders").withDeprecation("http://www.thisisdeprecated.com")),
      HalRelation("next", HalHref("/orders?page=2").withType("application/json")),
      HalRelation("find", HalHref("/orders{?id}", templated = true).withHreflang("de"))).json should equal(

        Json.parse("""{
        "_links": {
               "self": { "href": "/orders", "deprecation": "http://www.thisisdeprecated.com" },
               "next": { "href": "/orders?page=2", "type": "application/json" },
               "find": { "href": "/orders{?id}", "templated": true, "hreflang": "de" }
             }
        }""".stripMargin)
      )
  }

  test("provide support for arbitrary link attributes") {
    Hal.links(
      HalRelation("self", HalHref("/orders").withLinkAttributes(Json.obj("isRequired" -> true)))
    ).json should equal(

        Json.parse("""{
        "_links": {
               "self": { "href": "/orders", "isRequired": true }
             }
        }""".stripMargin)
      )
  }

  test("provide support for arbitrary link attributes (from seq)") {
    Hal.linksSeq(
      HalRelation("self", HalHref("/orders").withLinkAttributes(Json.obj("isRequired" -> true))) :: Nil
    ).json should equal(

        Json.parse("""{
        "_links": {
               "self": { "href": "/orders", "isRequired": true }
             }
        }""".stripMargin)
      )
  }
}
