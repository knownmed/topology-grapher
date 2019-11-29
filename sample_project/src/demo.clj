(ns demo
  (:require [topology-grapher.topics :as t]
            [topology-grapher.describe :as td]
            [describe :as d]))

(def topologies
  (td/gen-topologies (d/topologies) d/meta-data))

(comment
  (t/topology->loom
   (first
    (vals topologies))))

(comment
  (d/render-all!))
