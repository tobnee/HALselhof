package play.api.hal

import org.scalatestplus.play.PlaySpec
import play.api.hal.Hal._
import play.api.libs.json.Json

class HalBuilderSpec extends PlaySpec {

  "A HalBuilder" should {
    "build HAL resource with single relation" in {
      HalBuilder()
        .withRelation(
          "order",
          HalHref("/order")
        )
        .build()
        .json mustBe Json.parse("""{
            "_links": {
              "order" : { 
              "href"  : "/order"
              }
           }
        }""".stripMargin)
    }

    "build json with single relation" in {
      HalBuilder()
        .withRelation(
          "order",
          HalHref("/order")
        )
        .buildJson() mustBe Json.parse("""{
            "_links": {
              "order" : { 
              "href"  : "/order"
              }
           }
        }""".stripMargin)
    }

    "build json with single relation and optional link attributes" in {
      HalBuilder()
        .withRelation(
          "order",
          HalHref("/order")
            .withDeprecation("http://www.thisisdeprecated.com")
            .withType("application/json")
            .withHreflang("de")
            .withTemplated()
        )
        .buildJson() mustBe Json.parse("""{
            "_links": {
              "order" : { 
                "href"  : "/order",
                "deprecation": "http://www.thisisdeprecated.com",
                "type": "application/json",
                "hreflang": "de",
                "templated": true
              }
           }
        }""".stripMargin)
    }

    "build json with multiple single relations" in {
      HalBuilder()
        .withRelation(
          "self",
          HalHref("/orders")
        )
        .withRelation(
          "next",
          HalHref("/orders?page=2")
        )
        .withRelation(
          "find",
          HalHref("/orders{?id}")
            .withTemplated()
        )
        .buildJson() mustBe Json.parse("""{
            "_links": {
                "self": { "href": "/orders" },
                "next": { "href": "/orders?page=2" },
                "find": { "href": "/orders{?id}", "templated": true }
           }
        }""".stripMargin)
    }

    "build json with multiple relations" in {
      HalBuilder()
        .withRelation(
          "orders",
          Seq(
            HalHref(href = "/order/1").withName("1").withTemplated(),
            HalHref("/order/2").withName("2").withTemplated(),
            HalHref("/order/3").withName("3").withTemplated()
          )
        )
        .buildJson() mustBe Json.parse("""{
          "_links": {
            "orders" : [
              { "href": "/order/1", "name": "1", "templated": true},
              { "href": "/order/2", "name": "2", "templated": true},
              { "href": "/order/3", "name": "3", "templated": true}
          ]}
        }""".stripMargin)
    }
  }
}
