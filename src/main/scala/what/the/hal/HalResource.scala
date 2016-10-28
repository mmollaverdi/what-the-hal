package what.the.hal

import shapeless._

// The following, models a HAL Resource based on HAL specification:
// http://stateless.co/hal_specification.html

// A HAL Resource has some links, some state and a list of embedded resources.
// http://stateless.co/info-model.png
// Embedded resources can each have different types of state, hence the use of shapeless Heterogenous lists.
// The implicit LUBConstraint value puts a constraint on the elements of HList to be subtypes of HalEmbeddedResource.
case class HalResource[T, L <: HList](links: List[HalLink], state: T,
                                      embeddedResources: L = HNil)(implicit c: LUBConstraint[L, HalEmbeddedResource[_, _]])

// TODO Add support for link array. Can also be extended further to support templated links, as well as
// other link attributes such as name, title, type, etc.
case class HalLink(rel: String, href: String)

// Each embedded resource has a "rel" (relation) attribute which is used as the key name for that resource
// inside "_embedded" tag in a HAL resource.
case class HalEmbeddedResource[T, L <: HList](rel: String, embedded: EmbeddedResource[T, L])

// An embedded resource can be either a single resource (e.g. a single customer document embedded within an order document),
// or an array of resources (e.g. order items)
sealed trait EmbeddedResource[T, L]
case class SingleEmbeddedResource[T, L <: HList](embedded: HalResource[T, L]) extends EmbeddedResource[T, L]
case class ArrayEmbeddedResource[T, L <: HList](embedded: List[HalResource[T, L]]) extends EmbeddedResource[T, L]

object HalResource {
  // This provides the implicit evidence that an empty HList (HNil) contains only elements which are of type HalEmbeddedResource[_] !!!!!
  implicit val hnilLUBConstraint: LUBConstraint[HNil.type, HalEmbeddedResource[_, _]] =
    new LUBConstraint[HNil.type, HalEmbeddedResource[_, _]] {}
}
