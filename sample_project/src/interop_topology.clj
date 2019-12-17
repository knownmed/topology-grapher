(ns interop-topology
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
        (.to "interop-final"))

    (-> (.stream builder "topic-2")
        (.to "interop-final"))

    (.build builder)))
