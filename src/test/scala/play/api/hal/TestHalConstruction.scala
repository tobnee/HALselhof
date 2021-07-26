package play.api.hal

import org.scalatestplus.play.PlaySpec
import play.api.hal.Hal._
import play.api.libs.json.{ Json, OWrites }

class TestHalConstruction extends PlaySpec {

  case class TestData(total: Int, currency: String, status: String)
  implicit val testWrites: OWrites[TestData] = Json.writes[TestData]

  "A HAL relation" should {

    "render href as a JSON object" in {
      val data = HalHref("/orders")
      data.asResource.json mustBe Json.parse("""{
           "href": "/orders"
           }
       """.stripMargin)
    }

    "be a single link" in {
      val data: HalSingleRelation = HalRelation("order", HalHref("/order"))
      data.asResource.json mustBe Json.parse("""{
          "_links": {
          "order" : { "href": "/order"}
           }
           }""".stripMargin)
    }

    "render a link to share relation with another link" in {
      Hal
        .links(
          HalRelation("orders", List(HalHref("/orders/1"), HalHref("/orders/2"))),
          HalRelation("find", "/search{?id}", templated = true)
        )
        .json mustBe Json.parse("""{
          "_links": {
          "orders" : [{ "href": "/orders/1"}, { "href": "/orders/2"}],
          "find":{"href":"/search{?id}","templated":true}
           }
           }""".stripMargin)
    }
  }

  "A HAL resource" should {

    "be a JSON object" in {
      val data = TestData(20, "EUR", "shipped")
      data.asResource.json mustBe Json.toJson(data)
    }

    "contain only links" in {
      Hal
        .links(
          HalRelation("self", "/orders"),
          HalRelation("next", "/orders?page=2"),
          HalRelation("find", "/orders{?id}", templated = true)
        )
        .json mustBe Json.parse("""{
                       "_links": {
                       "self": { "href": "/orders" },
                       "next": { "href": "/orders?page=2" },
                       "find": { "href": "/orders{?id}", "templated": true }
                        }
                        }""".stripMargin)
    }

    "contain links and state" in {
      val data = TestData(20, "EUR", "shipped")
      (data.asResource ++
        HalRelation("self", "/orders") ++
        HalRelation("next", "/orders?page=2") ++
        HalRelation("find", "/orders{?id}", templated = true)).json mustBe
        Json.parse("""{
                       "_links": {
                       "self": { "href": "/orders" },
                       "next": { "href": "/orders?page=2" },
                       "find": { "href": "/orders{?id}", "templated": true }
                        },
                         "total" : 20,
                         "currency" : "EUR",
                         "status": "shipped"
                        }""".stripMargin)
    }

    "embed links" in {
      val json = TestData(20, "EUR", "shipped")
      Json.toJson(Hal.state(json))
      val selfLink = HalRelation("self", "/blog-post").asResource
      val authorLink = HalRelation("author", "/people/alan-watts")
      val embeddedAuthorState = Json
        .obj("name" -> "Alan Watts", "born" -> "January 6, 1915", "died" -> "November 16, 1973")
        .asResource

      (selfLink ++ Hal.embeddedLink(authorLink, embeddedAuthorState)).json mustBe
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
                  }""".stripMargin)
    }

    "embed multiple resources" in {
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

      res.json mustBe Json.parse("""{
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
      }""".stripMargin)
    }

    "provide alternative names for composition" in {
      val data = TestData(20, "EUR", "shipped")
      data.asResource ++
        HalRelation("self", "/orders") ++
        HalRelation("next", "/orders?page=2") ++
        HalRelation("find", "/orders{?id}", templated = true) mustBe
        (data.asResource include
          HalRelation("self", "/orders") include
          HalRelation("next", "/orders?page=2") include
          HalRelation("find", "/orders{?id}", templated = true))
    }

    "provide support for optional link attributes" in {
      Hal
        .links(
          HalRelation("self", HalHref("/orders").withDeprecation("http://www.thisisdeprecated.com")),
          HalRelation("next", HalHref("/orders?page=2").withType("application/json")),
          HalRelation("find", HalHref("/orders{?id}", templated = true).withHreflang("de"))
        )
        .json mustBe Json.parse("""{
        "_links": {
               "self": { "href": "/orders", "deprecation": "http://www.thisisdeprecated.com" },
               "next": { "href": "/orders?page=2", "type": "application/json" },
               "find": { "href": "/orders{?id}", "templated": true, "hreflang": "de" }
             }
        }""".stripMargin)
    }

    "provide support for arbitrary link attributes" in {
      Hal
        .links(HalRelation("self", HalHref("/orders").withLinkAttributes(Json.obj("isRequired" -> true))))
        .json mustBe Json.parse("""{
        "_links": {
               "self": { "href": "/orders", "isRequired": true }
             }
        }""".stripMargin)
    }

    "provide support for arbitrary link attributes (from seq)" in {
      Hal
        .linksSeq(HalRelation("self", HalHref("/orders").withLinkAttributes(Json.obj("isRequired" -> true))) :: Nil)
        .json mustBe Json.parse("""{
        "_links": {
               "self": { "href": "/orders", "isRequired": true }
             }
        }""".stripMargin)
    }
  }
}
