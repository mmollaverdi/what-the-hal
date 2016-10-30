# what-the-hal

## Intro

This repository models a HAL Resource based on [HAL specification](http://stateless.co/hal_specification.html) and provides [Argonaut](http://argonaut.io/) JSON encoders for the models (Argonaut is a purely functional Scala JSON library).
This was originally implemented a while ago and used in a Scala micro service at REA Group for generating HAL JSON responses. I had published it in a public github gist before [here](https://gist.github.com/mmollaverdi/de79ede5d9054f75b72a), but I've now open sourced it in this repository for further exposure.

A HAL Resource contains state, links and a collection of embedded resources:
![HAL Resource](http://stateless.co/info-model.png)

A property listing for example can be represented in HAL JSON format as below:

```json
{
  "id" : "1",
  "address" : "511 Church St, Richmond",
  "_links" : {
    "self" : {
      "href" : "/property/1"
    }
  },
  "_embedded" : {
    "image" : {
      "title" : "Floor Plan",
      "_links" : {
        "self" : {
          "href" : "/image/1"
        }
      }
    },
    "listers" : [
      { 
        "id" : "1",
        "name" : "Jim Smith",
        "_links" : {
          "self" : {
            "href" : "/lister/1"
          }
        },
        "_embedded" : {
          "agency" : {
            "id" : "1",
            "name" : "Ray White",
            "address" : "Hawthorn",
            "_links" : {
              "self" : {
                "href" : "/agency/1"
              }
            }
          }
        }
      },
      {
        "id" : "2",
        "name" : "Joe Bird",
        "_links" : {
          "self" : {
            "href" : "/lister/2"
          }
        },
        "_embedded" : {
          "agency" : {
            "id" : "1",
            "name" : "Ray White",
            "address" : "Hawthorn",
            "_links" : {
              "self" : {
                "href" : "/agency/1"
              }
            }
          }
        }
      }
    ]
  }
}
```

In this example
- `id` and `address` are the property listing "state".  
- The listing has a `self` "link".
- `image` and `listers` are "embedded" HAL resources.

Each embedded resource is a HAL resource itself, hence the need for a recursive data structure. In the example above, `image` is an embedded resource which has a `title` as "state" and a `self` "link", but no nested embedded resources.

Different embedded resource within a HAL resource can have different types of state. In the example above, `image` and `listers` are embedded resources with different state (data) types. I've used [Shapeless](https://github.com/milessabin/shapeless) [Heterogenous lists](https://github.com/milessabin/shapeless/wiki/Feature-overview:-shapeless-2.0.0#heterogenous-lists) to preserve the type of each embedded resource.

## How to use

1. First, define different type of States which you need in your HAL resource and embedded resources. For instance, for the example JSON above: 

  ```scala
  case class Property(id: String, address: String)
  case class Lister(id: String, name: String)
  case class Image(title: String)
  case class Agency(id: String, name: String, address: String)
  ```

2. Provide Argonaut encoders for those types:

  ```scala
  object StateJsonEncoders {

    implicit def PropertyEncoder = EncodeJson[Property] { p => ("id" := p.id) ->: ("address" := p.address) ->: jEmptyObject }

    implicit def ListerEncoder = EncodeJson[Lister] { a => ("id" := a.id) ->: ("name" := a.name) ->: jEmptyObject }

    implicit def ImageEncoder = EncodeJson[Image] { i => ("title" := i.title) ->: jEmptyObject }

    implicit def AgencyEncoder = EncodeJson[Agency] { a => ("id" := a.id) ->: ("name" := a.name) ->: ("address" := a.address) ->: jEmptyObject }
  }
  ```

3. Instantiate the HAL resource object that you intend to generate JSON for:

  ```scala
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
  ```

4. Use Argonaut to generate your HAL JSON String:

  ```scala
  val json = halResource.asJson.nospaces
  ```

## TODOs

- Add [circe library](https://github.com/travisbrown/circe) integration 
- Implement JSON decoders
