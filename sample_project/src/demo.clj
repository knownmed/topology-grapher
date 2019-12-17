(ns demo
  (:require [topology-grapher.describe :as td]
            [topology-grapher.topics :as t]
            [describe :as d]))

(def topologies
  (td/gen-topologies (d/topologies) d/meta-data))

(def sample-topology (first (vals topologies)))

;; query information about the topologies
(comment
  (t/topology->loom sample-topology))
;; => #loom.graph.BasicEditableDigraph{:nodeset #{"output" "input" "my-application-id"}, :adj {"input" #{"my-application-id"}, "my-application-id" #{"output"}}, :in {"my-application-id" #{"input"}, "output" #{"my-application-id"}}}
;; => nil

;; now extract the topics
(comment
  (t/extract-topics sample-topology))
;; => {:inputs #{"input"}, :outputs #{"output"}}

;; render all the topologies available
(comment
  (d/render-all!))
;; => "/tmp/graphs/detail_26671937151c7d7fec4c545264f060bc.png"
