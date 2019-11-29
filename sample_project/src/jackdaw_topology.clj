(ns jackdaw-topology
  (:require [jackdaw.streams :as js]
            [jackdaw.serdes.edn :as je]))

;;reate a more meaningful example using some joins and internal stores as well
(defn topic-config
  "Takes a topic name and returns a topic configuration map, which may
  be used to create a topic or produce/consume records."
  [topic-name]
  {:topic-name topic-name
   :partition-count 1
   :replication-factor 1
   :key-serde (je/serde)
   :value-serde (je/serde)})

(defn t1
  [builder]
  (-> (js/kstream builder (topic-config "input"))
      (js/filter (fn [v] (not (:enabled v))))
      (js/map-values (fn [v] (update v :field inc)))
      (js/to (topic-config "output")))

  builder)

(defn t2
  [builder]
  (-> (js/kstream builder (topic-config "output"))
      (js/filter (fn [v] (not (:added v))))
      (js/to (topic-config "final")))
  builder)
