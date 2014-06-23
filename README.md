# HALselhof
A [HAL](http://tools.ietf.org/html/draft-kelly-json-hal) library based on [Play-JSON](https://www.playframework.com/documentation/2.3.x/ScalaJson). 

## Example
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

// transfer the resource to the Play Json AST
val json = resource.json
```
