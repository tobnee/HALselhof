# HALselhof
A [HAL](http://tools.ietf.org/html/draft-kelly-json-hal) library based on [Play-JSON](https://www.playframework.com/documentation/2.3.x/ScalaJson). 

[![CI](https://github.com/vangogiel/HALselhof/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/vangogiel/HALselhof/actions/workflows/build.yml)

## Standalone Example
```scala
// Test data which should be reflected in the resource state
case class TestData(total: Int, currency: String, status: String)

// a generated play.api.libs.json.Writes
implicit val testWrites = Json.writes[TestData]

// transfer the resource state into a full HAL resource
val data = TestData(20, "EUR", "shipped")
val resource: HalResource = data.asResource ++
 HalLink("self", "/orders") ++
 HalLink("next", "/orders?page=2") ++
 HalLink("find", "/orders{?id}", templated = true)

// transfer the resource to the Play JSON AST
val json = resource.json
```
[Examples](https://github.com/tobnee/HALselhof/blob/master/src/test/scala/play/api/hal/TestHalConstruction.scala)

## Play Framework Integration

```scala
// within a Play Controller HAL resources can be serialized directly and are supported within content negotiation

import play.api.hal._
import play.api.mvc.hal._

def halOrJson = Action { implicit request =>
  render {
    case Accepts.Json() => Ok(Json.obj("foo" -> "bar"))
    case AcceptHal() => Ok(Hal.state(Json.obj("foo" -> "bar")) ++ HalRelation("self", "/foo"))
  }
}
```
