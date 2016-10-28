package what.the.hal.argonaut

import what.the.hal.{HalResource, HalLink, HalEmbeddedResource, SingleEmbeddedResource, ArrayEmbeddedResource}
import argonaut._, Argonaut._
import shapeless._
import shapeless.ops.hlist.{ToTraversable, Mapper}

object ArgonautJsonEncoders {
  private def halLinkJsonAssoc: HalLink => JsonAssoc = {
    case HalLink(rel, href) => rel := Json.obj("href" := href)
  }

  implicit def HalLinkJsonEncoder: EncodeJson[HalLink] = EncodeJson[HalLink] {
    halLink => halLinkJsonAssoc(halLink) ->: jEmptyObject
  }

  object HalEmbeddedResourceJsonAssoc extends Poly1 {
    implicit def default[T: EncodeJson, L <: HList, H[U, M <: HList] <: HalEmbeddedResource[U, M]]
        (implicit halResourceEncoder: EncodeJson[HalResource[T, L]]) = at[H[T, L]] {
      halEmbeddedResource => {
        halEmbeddedResource match {
          case HalEmbeddedResource(rel, SingleEmbeddedResource(embedded)) => rel := embedded
          case HalEmbeddedResource(rel, ArrayEmbeddedResource(embedded)) => rel := embedded
        }
      }
    }
  }

  implicit def HalEmbeddedResourceJsonEncoder[T: EncodeJson, L <: HList]
      (implicit halResourceEncoder: EncodeJson[HalResource[T, L]]): EncodeJson[HalEmbeddedResource[T, L]] =
    EncodeJson[HalEmbeddedResource[T, L]] {
      halEmbeddedResource => HalEmbeddedResourceJsonAssoc(halEmbeddedResource) ->: jEmptyObject
    }

  implicit def HalResourceWithNoEmbeddedResourcesJsonEncoder[T: EncodeJson, L <: HNil]
      : EncodeJson[HalResource[T, L]] = EncodeJson[HalResource[T, L]] {
    halResource => {
      val linksJson = jObjectAssocList(halResource.links.map(halLinkJsonAssoc))
      val stateJsonAssociations = implicitly[EncodeJson[T]].apply(halResource.state).assoc.getOrElse(List())
      Json.obj(("_links" -> linksJson :: stateJsonAssociations): _*)
    }
  }

  implicit def HalResourceWithEmbeddedResourcesJsonEncoder[T: EncodeJson, L <: HList, M <: HList]
      (implicit m: Mapper[HalEmbeddedResourceJsonAssoc.type, L] { type Out = M},
   n: ToTraversable.Aux[M , List, JsonAssoc]): EncodeJson[HalResource[T, L]] = EncodeJson[HalResource[T, L]] {
    halResource => {
      val embeddedResourcesJson = jObjectAssocList(halResource.embeddedResources.map(HalEmbeddedResourceJsonAssoc).toList)
      val linksJson = jObjectAssocList(halResource.links.map(halLinkJsonAssoc))
      val stateJsonAssociations = implicitly[EncodeJson[T]].apply(halResource.state).assoc.getOrElse(List())
      Json.obj(("_embedded" -> embeddedResourcesJson :: "_links" -> linksJson :: stateJsonAssociations): _*)
    }
  }
}
