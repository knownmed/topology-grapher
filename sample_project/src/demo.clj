(ns demo
  (:require [topology-grapher.topics :as t]
            [topology-grapher.describe :as td]
            [describe :as d]))

(def topologies
  (td/gen-topologies (d/topologies) d/meta-data))

(def sample-topology (first (vals topologies)))

;; query information about the topologies
(comment
  (t/topology->loom sample-topology))

;; now extract the topics
(comment
  (t/extract-topics sample-topology))

;; render all the topologies available
(comment
  (d/render-all!))
