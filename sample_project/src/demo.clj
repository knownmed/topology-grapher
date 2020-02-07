(ns demo
  (:require [topology-grapher.describe :as td]
            [topology-grapher.render :as tr]
            [jackdaw.streams :as js]
            [jackdaw.serdes.edn :as je]))

(defn topic-config
  "Takes a topic name and returns a topic configuration map, which may
  be used to create a topic or produce/consume records."
  [topic-name]
  {:topic-name topic-name
   :partition-count 1
   :replication-factor 1
   :key-serde (je/serde)
   :value-serde (je/serde)})

(defn transform
  [v]
  (-> v
      (update :value inc)
      (select-keys [:value])))

(defn t1
  [builder]
  (-> (js/kstream builder (topic-config "input"))
      (js/filter (fn [v] (not (:enabled v))))
      (js/map-values transform)
      (js/to (topic-config "output")))

  builder)

(def meta-data
  {:domain "Big Corp"
   :subdomain "departement"
   :application "sample"})

(defn topology-from-stream-builder
  [stream-builder]
  (.build (js/streams-builder* stream-builder)))

;; now we define a list of topologies to render, in this case just t1
(def topologies
  [{:topology (topology-from-stream-builder (t1 (js/streams-builder)))
    :application-name "my-application-id"}])

(def topology-edn (td/gen-topologies topologies meta-data))

(comment
  (tr/render-graph (vals topology-edn) {:fmt "png" :mode "topics" :cache false}))
;; => "/tmp/graphs/topics_c591fd52eef60efa56b0cd513aac14c1.png"
