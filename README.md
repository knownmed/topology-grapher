[![Clojars Project](https://img.shields.io/clojars/v/fundingcircle/topology-grapher.svg)](https://clojars.org/fundingcircle/topology-grapher)
[![CircleCI](https://circleci.com/gh/FundingCircle/topology-grapher/tree/master.svg?style=svg)](https://circleci.com/gh/FundingCircle/topology-grapher/tree/master)
[![codecov](https://codecov.io/gh/FundingCircle/topology-grapher/branch/master/graph/badge.svg)](https://codecov.io/gh/FundingCircle/topology-grapher)

# Topology Grapher

This library provides a means to build a directed graph (data) from a kafka streams topology.

## How to integrate into your application

If you want to play around with this library you can check the [sample project](./sample_project/README.md).

There are a few simple steps to follow to integrate your kafka streaming app.

## 1. implement a command to call the describe-topologies

```
(ns your-namespace
  (:require [topology-grapher.describe :refer [generate-zip]]))

(def meta-data {:domain "domain"
    :subdomain "subdomain"
    :application "application-name"})

(def topologies
  [{:application-name "app-2"
    :topology topology-1}

   {:application-name "app-1"
    :topology topology-2}])

(generate-zip topologies meta-data)

```

The `generate-zip` function is what takes care of transforming
topologies objects into EDN files that are written out to disk.

The important thing is that you need to be have a function that
returns a topology object *without* having to actually start Kafka.

This is normally fine, but if you are using something like the Stuart
Sierra [component](https://github.com/stuartsierra/component) for example you will need to refactor the
initialisation to avoid needing the full system started.

## 2. add a call to that command in CircleCI

This is an example of configuration you need to add to CircleCI to generate and publish graph information.

```
  publish_topologies_graphs:
    docker:
      - image: *lein

    steps:
      - checkout
      - run: sudo apt update && sudo apt install awscli
      - restore_cache:
          key: your-project-{{ checksum "project.clj" }}

      # the precise command depends on how you integrated
      - run: lein run describe-topologies
      # not the real bucket yet since we are still waiting for one
      # - run: aws s3 cp --recursive /tmp/graphs/$your-project-name s3://$your-bucket
```

This particular CircleCI step can run in parallel with any other job, and should not be required as dependency.

And you are done, if you integrated in a way that all the topologies will always be published automatically
there is 0 maintenance effort needed from now on.

# Graph implementation

Although the generation of graph data is currently Kafka Streams specific, the
generated graph data is generic and the rest of the functionality operates only
on the graph data. The intended flow of operation is:

```
    Data Source (e.g. Kafka Stream)
        |
        v
    Graph Generator (specific to source)
        |
        v
    Graph data (generic)
```

The graph generation has been tested on Kafka 1.0+ streams apps, both using the
higher level DSL and lower level streams APIs.

## Internal graph format

The raw graph data is modeled in the form of Clojure maps. A topology
represents the top level construct, and consists of a set of sub-graphs. The
minimum data returned is described below. Each map my be further augmented with
extra meta data as required.

```
{
 :topology <the name for this topology, e.g. a consumer group name>
 :id       <a unique UUID for this topology, deterministic from its name>
 :graphs   <a list of all the graphs in the topology>
}
```

Where each graph is:

```
{
 :type  <the type of the thing the graph represents>
 :name  <the name that kafka gives this stream>
 :id    <a unique UUID for the graph deterministic from the enclosing topology
         and this graphs name>
 :nodes <a list of all the nodes in the graph>
 :edges <a list of all the edges in the graph>i
}
```

... And edges and nodes are represented as:

```
{
 :id   <a deterministic UUID for the node>
 :name <the 'human' name for the node>
 :type <the type for the node - processor, store, topic &c.>i
}

{
 :from    <the :name of the node the edge comes from>
 :from-id <the :id of the node the edge comes from>
 :to      <the :name of the node the edge goes to>
 :to-id   <the :id of the node the edge goes to>
}
```

All Ids are generated using v5 UUIDs, and are guaranteed globally unique where
objects are distinct, and guaranteed globally equal where objects are the same.
This is to allow subgraphs to me merged as required the generate larger sets of
graph data for analysis / rendering without Id clashes.

Topics, for example, are always given a UUID from a single global namespace so
that a topic node for the same topic will have the same UUID regardless of which
system generates a graph. Currently all other nodes receive a unique Id.

For example, here is the data for a simple single-stream (so one sub graph):

```
{:domain "marketplace",
 :subdomain "servicing",
 :application "puma",
 :topology "1-product-unit-manager.topologies.publish-loan-part-1",
 :id #uuid "5b340651-fa64-53fa-a3b6-8efa5987bf12",
 :graphs
 ({:type :stream,
   :name "stream-0",
   :id #uuid "2c7000f8-d9d5-5b99-be2f-48f5fc035fa9",
   :nodes
   ({:type :topic,
     :name "product-unit-pre-release-11",
     :id #uuid "cff44820-a2a7-5cea-b1c6-66acc0d725eb"}
    {:type :source,
     :name "KSTREAM-SOURCE-0000000000",
     :id #uuid "f1358ed5-a692-5fb5-b240-eb5922ee1643"}
    {:type :processor,
     :name "KSTREAM-MAPVALUES-0000000001",
     :id #uuid "c1db8077-5694-5750-9eb5-98380a3acc39"}
    {:type :sink,
     :name "KSTREAM-SINK-0000000002",
     :id #uuid "b3f4ee01-7f3d-59f6-84cf-d26f877b6a19"}
    {:type :topic,
     :name "loan-part-1",
     :id #uuid "f531da79-1bc8-56fa-b76a-3556d29b1e33"}),
   :edges
   ({:from "KSTREAM-SOURCE-0000000000",
     :to "KSTREAM-MAPVALUES-0000000001",
     :from-id #uuid "f1358ed5-a692-5fb5-b240-eb5922ee1643",
     :to-id #uuid "c1db8077-5694-5750-9eb5-98380a3acc39"}
    {:from "KSTREAM-SINK-0000000002",
     :to "loan-part-1",
     :from-id #uuid "b3f4ee01-7f3d-59f6-84cf-d26f877b6a19",
     :to-id #uuid "f531da79-1bc8-56fa-b76a-3556d29b1e33"}
    {:from "product-unit-pre-release-11",
     :to "KSTREAM-SOURCE-0000000000",
     :from-id #uuid "cff44820-a2a7-5cea-b1c6-66acc0d725eb",
     :to-id #uuid "f1358ed5-a692-5fb5-b240-eb5922ee1643"}
    {:from "KSTREAM-MAPVALUES-0000000001",
     :to "KSTREAM-SINK-0000000002",
     :from-id #uuid "c1db8077-5694-5750-9eb5-98380a3acc39",
     :to-id #uuid "b3f4ee01-7f3d-59f6-84cf-d26f877b6a19"}))}
```

# Run, Test, Deploy

This is a straightforward Leiningen project with (deliberately) minimal
dependencies.

* `lein repl`
* `lein test`
* `lein install` (to install a local snapshot for any local integration testing)

# Release

This project uses [lein-git-version](https://github.com/arrdem/lein-git-version) for the versioning, which means that the current version is fetched from the git tags.

To release a new version you just need to:
- create a new `git tag`, following [Semantic Versioning](https://semver.org/) conventions (check the changes since last version to decide what kind of release this is)
- run `lein deploy` to do the release


# Contributing

We welcome any thoughts or patches, please check the [Contributing guide](CONTRIBUTING.md) to see how to contribute to this project.
