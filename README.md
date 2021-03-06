[![Build Status](https://snap-ci.com/ashwanthkumar/finder/branch/master/build_image)](https://snap-ci.com/ashwanthkumar/finder/branch/master)

# finder
Finder is a hobby project to build a [Wayback](https://github.com/iipc/openwayback/) like system without converting our existing crawled data which are in SequenceFiles to WARC / ARC formats. Architecture is very much inspired from Wayback but with a few changes. Finder not only supports HTML but can provide key+timestamp based access to any dataset. These are very typical in the [λ Architecture](http://lambda-architecture.net/) world, where raw immutable dataset is the fundamental assumption.

## Getting Started
* Run the `SampleDatagenerator` program from your fav. IDE
* By default it would create a folder in the project structure called "example-user-data"
* Run the `FinderService` from `services` module
* Example Queries
  * [http://localhost:7070/user/search/Tanek](http://localhost:7070/user/search/Tanek)
  * [http://localhost:7070/user/get/Tanek/timestamp_from_previous_search](http://localhost:7070/user/get/Tanek/timestamp_from_previous_search)

## Dependencies
To build datasets for your models, use the following dependency on finder-core.

For Maven,
```xml
<dependency>
  <groupId>in.ashwanthkumar</groupId>
  <artifactId>finder-core_2.10</artifactId>
  <version>0.0.1</version>
</dependency>
```

For SBT,
```sbt
libraryDependencies += "in.ashwanthkumar" %% "finder-core" % "0.0.1"
```


## Finder HTTP Service
Finder comes with a simple microservice that gives you access to the dataset.
```
GET  /:dataset/search/:key  Searches for a given key in the given dataset. Key is prefix matched with the key that's used in the dataset while indexing.
GET  /:dataset/get/:key/:timestamp Returns the underlying record from the Dataset.
```

## More Info
You can define a custom Dataset like
```scala
import finder.spec.Dataset
import finder.util.JSONUtil

case class User(name: String, age: Int, timestamp: Long)

class UserDataset extends Dataset[User] {
  override def key(input: User): String = input.name
  /**
   * Timestamp of the record in UTC timezone
   */
  override def timestamp(input: User): Long = input.timestamp
  override def serialize(input: User): Array[Byte] = JSONUtil.toJson(input).getBytes
  override def deserialize(input: Array[Byte]): User = JSONUtil.readAs(classOf[User])(new String(input))
}
```

All the dataset goes with a configuration in `application.conf` like
```hocon
finder {
  datasets {
    descriptors: [{
      name: "user"
      impl: "finder.models.UserDataset"
      index {
        params {
          db-location: "user-db"
        }
      }
    }]
  }
}
```

Finder uses an Index to store and retrieve metadata information regarding every record. By default we use LevelDB backed index, but again it is pluggable. Most likely we could use something like [ElephantDB](https://github.com/nathanmarz/elephantdb) to query the index.


## TODOs
- <s>Build a HTTP service to provide access to these datasets</s>
- ElephantDB Adaptors
- Partitioned Indexes - Comes for free with ElephantDB but for local LevelDB instances we might want to build something on our own
- Versioned datasets - Each version could be a different run of the view in Batch Layer

## License
Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
