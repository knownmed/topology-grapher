(ns topology-grapher.topics
  "Logic to extract topics information from the graph"
  (:require [loom.graph :as lg]
            [clojure.set :as s]
            [topology-grapher.analytics :as a]))

(defn topology->loom
  "Convert the given topology graph into a loom graph,
  which makes it simpler to do further analysis later on."
  [topology]
  (let [g (-> topology
              a/prune-to-topology)
        tuples (apply concat
                      (for [g (:graphs g)]
                        (for [e (:edges g)]
                          [(:from e) (:to e)])))]
    (apply lg/digraph tuples)))

(defn boundaries
  [gr mode]
  (let [in-out-fn (if (= mode :in) lg/in-edges lg/out-edges)
        edges (into {}
                    (for [n (lg/nodes gr)]
                      {n (in-out-fn gr n)}))]
    (->> edges
         (filter #(empty? (second %)))
         (map first)
         set)))

(defn extract-topics
  [topology]
  (let [gr (topology->loom topology)]
    ;; could also add intermediate topics potentially?
    ;; which are all the topics with in and out edges?
    {:inputs (boundaries gr :in)
     :outputs (boundaries gr :out)}))

(defn topics-map->topics
  [topics-map]
  (let [vs (vals topics-map)
        inp (map :inputs vs)
        out (map :outputs vs)]
    (s/union
     (reduce s/union inp)
     (reduce s/union out))))

(defn topics-by-topology
  [mode topics]
  (let [ts (for [t (topics-map->topics topics)]
             (for [[id v] topics]
               (when (contains? (mode v) t)
                 [t id])))
        rr (->> ts
                (apply concat)
                (filter some?))]
    (into {}
          (for [[k v] (group-by first rr)]
            {k (map second v)}))))
