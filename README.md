# what-the-hal

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
