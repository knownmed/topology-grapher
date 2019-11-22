(ns topology-grapher.topology
  (:gen-class)
  (:require [topology-grapher.describe :refer [describe-all]])
  (:import (org.apache.kafka.streams StreamsBuilder KafkaStreams)
           (org.apache.kafka.streams.kstream KStream)
           (org.apache.kafka.streams.kstream Predicate ValueMapper)))

(defn f->Predicate
  [f]
  (reify Predicate
    (test [_ k v]
      (f k v))))

(defn f->ValueMapper
  [f]
  (reify ValueMapper
    (apply [_ v]
      (f v))))

(defn gen-topology
  []
  (let [builder (StreamsBuilder.)]
    (-> (.stream builder "topic-1")
        (.filter (f->Predicate (fn [k v] (pos? v))))
        (.mapValues (f->ValueMapper (fn [v] (inc v))))
        (.to "stream1-topic"))

    (-> (.stream builder "topic-2")
        (.to "stream2-topic"))

    (.build builder)))

(def application-name "test-application")

(def topologies
  [{:fn (fn [_] (gen-topology))
    :config {"application.id" application-name}}])

(def meta-data
  {:application application-name
   :domain "world"})

(defn -main
  [& args]
  (describe-all topologies meta-data))
