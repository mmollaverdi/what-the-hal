package what.the.hal.argonaut

import argonaut._, Argonaut._
import org.specs2.matcher.JsonMatchers
import org.specs2.mutable.Specification
import shapeless._
import what.the.hal._
import ArgonautJsonEncoders._
import what.the.hal.HalResource._

case class Property(id: String, address: String)
case class Lister(id: String, name: String)
case class Image(title: String)
case class Agency(id: String, name: String, address: String)

object StateJsonEncoders {

  implicit def PropertyEncoder = EncodeJson[Property] { p => ("id" := p.id) ->: ("address" := p.address) ->: jEmptyObject }

  implicit def ListerEncoder = EncodeJson[Lister] { a => ("id" := a.id) ->: ("name" := a.name) ->: jEmptyObject }

  implicit def ImageEncoder = EncodeJson[Image] { i => ("title" := i.title) ->: jEmptyObject }

  implicit def AgencyEncoder = EncodeJson[Agency] { a => ("id" := a.id) ->: ("name" := a.name) ->: ("address" := a.address) ->: jEmptyObject }
}

class ArgonautJsonEncodersSpec extends Specification with JsonMatchers {

  "A HalLink" should {
    "be encoded with the rel as the parent attribute name" in {
      val halLink = HalLink(rel = "self", href = "/link-to-somewhere")

      halLink.asJson.nospaces must / ("self") / ("href" -> "/link-to-somewhere")
    }
  }

  "A singular HalEmbeddedResource" should {
    "be encoded with the rel as the parent attribute name" in {
      import StateJsonEncoders._

      val embeddedLister = HalResource(links = List(new HalLink("self", "/lister/1")), state = Lister("1", "Jim Smith"))
      val halEmbeddedResource = HalEmbeddedResource(rel = "item", embedded = SingleEmbeddedResource(embeddedLister))

      halEmbeddedResource.asJson.nospaces must
        / ("item") / "_links" / "self" / ("href" -> "/lister/1") and
        / ("item") / "id" / "1" and
        / ("item") / "name" / "Jim Smith"
    }
  }

  "An array HalEmbeddedResource" should {
    "be encoded as an array with the rel as the parent attribute name" in {
      import StateJsonEncoders._

      val embeddedOne = HalResource(links = List(new HalLink("self", "/lister/1")), state = Lister("1", "Jim Smith"))
      val embeddedTwo = HalResource(links = List(new HalLink("self", "/lister/2")), state = Lister("2", "Joe Bird"))
      val halEmbeddedResource = HalEmbeddedResource(rel = "listers", embedded = ArrayEmbeddedResource(
        List(embeddedOne, embeddedTwo)))

      halEmbeddedResource.asJson.nospaces must
        / ("listers") /# 0 / "_links" / "self" / ("href" -> "/lister/1") and
        / ("listers") /# 0 / ("id" -> "1") and
        / ("listers") /# 1 / "_links" / "self" / ("href" -> "/lister/2") and
        / ("listers") /# 1 / ("id" -> "2")
    }
  }

  "A HalResource" should {
    import StateJsonEncoders._

    "be encoded when there are no embedded resources" in {
      val halResource = HalResource(links = List(new HalLink("self", "/property/1")),
        state = Property("1", "511 Church St, Richmond"), embeddedResources = HNil)

      val json = halResource.asJson.spaces2
      json must
        / ("_links") / "self" / ("href" -> "/property/1") and
        / ("id" -> "1") and
        / ("address" -> "511 Church St, Richmond")
    }

    "have everything encoded with 2 levels of embedded resources" in {
      val secondLevelEmbedded = HalResource(links = List(new HalLink("self", "/agency/1")),
        state = Agency("1", "Ray White", "Hawthorn"))
      val halSecondLevelEmbeddedResource = HalEmbeddedResource(rel = "agency", embedded = SingleEmbeddedResource(
        secondLevelEmbedded))

      val embeddedOne = HalResource(links = List(new HalLink("self", "/lister/1")), state = Lister("1", "Jim Smith"),
        embeddedResources = halSecondLevelEmbeddedResource :: HNil)
      val embeddedTwo = HalResource(links = List(new HalLink("self", "/lister/2")), state = Lister("2", "Joe Bird"),
        embeddedResources = halSecondLevelEmbeddedResource :: HNil)
      val halEmbeddedResourceOne = HalEmbeddedResource(rel = "listers", embedded = ArrayEmbeddedResource(
        List(embeddedOne, embeddedTwo)))

      val embeddedThree = HalResource(links = List(new HalLink("self", "/image/1")), state = Image("Floor Plan"))
      val halEmbeddedResourceTwo = HalEmbeddedResource(rel = "image", embedded = SingleEmbeddedResource(embeddedThree))

      val halResource = HalResource(links = List(new HalLink("self", "/property/1")),
        state = Property("1", "511 Church St, Richmond"),
        embeddedResources = halEmbeddedResourceOne :: halEmbeddedResourceTwo :: HNil)

      val json = halResource.asJson.nospaces
      json must
        / ("_links") / "self" / ("href" -> "/property/1") and
        / ("id" -> "1") and
        / ("address" -> "511 Church St, Richmond") and
        / ("_embedded") / "listers" /# 0 / "_links" / "self" / ("href" -> "/lister/1") and
        / ("_embedded") / "listers" /# 0 / ("id" -> "1") and
        / ("_embedded") / "listers" /# 0 / ("name" -> "Jim Smith") and
        / ("_embedded") / "listers" /# 0 / "_embedded" / "agency" / ("id" -> "1") and
        / ("_embedded") / "listers" /# 1 / "_links" / "self" / ("href" -> "/lister/2") and
        / ("_embedded") / "listers" /# 1 / ("id" -> "2") and
        / ("_embedded") / "listers" /# 1 / ("name" -> "Joe Bird") and
        / ("_embedded") / "listers" /# 1 / "_embedded" / "agency" / ("id" -> "1") and
        / ("_embedded") / "image" / "_links" / "self" / ("href" -> "/image/1") and
        / ("_embedded") / "image" / ("title" -> "Floor Plan")
    }
  }
}
