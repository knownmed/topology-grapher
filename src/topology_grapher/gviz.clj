(ns topology-grapher.gviz
  (:require [clojure.string :as s]
            [clojure.java.shell :as sh]
            [clojure.java.io :as io]
            [topology-grapher.analytics :as ga]))

(defn gviz-safe [s]
  (let [s* (-> (str s)
               (s/replace "-" "_")
               (s/replace "." "_")
               (s/replace " " "_"))]
    (if (re-matches #"^[0-9].*" s*)
      (str "n" s*)
      s*)))

(defn clean-kafka-name [s]
  (if-let [[[_ n]] (re-seq #"KSTREAM-(.*)-[0-9]+" s)]
    n
    s))

(defn gviz-label [n]
  (str "label=" (gviz-safe (clean-kafka-name (:name n)))))

(def default-style
  {:shape :box
   :style :filled
   :fillcolor :gray70})

(def styles
  {:node {}
   :source {}
   :sink {}
   :processor {:fillcolor :lightblue
               :shape :parallelogram}
   :topic {:fillcolor :green2
           :shape :cds}
   :store {:fillcolor :lightblue3
           :shape :cylinder}
   :stream {:shape :parallelogram
            :fillcolor :lightblue}
   :topology {:shape :component
              :fillcolor :lightblue}})

(defn render-el
  [n]
  (let [el-type (:type n)
        el-style (merge default-style (el-type styles))]
    (str (gviz-safe (:id n))
         " ["
         (s/join ";"
                 (cons (gviz-label n)
                       (for [[k v] el-style]
                         (format "%s=%s" (name k) (name v)))))
         "];")))

(defn render-edge
  [e]
  (str (gviz-safe (:from-id e))
       ":e -> "
       (gviz-safe (:to-id e))
       ":w;"))

;; Public API

(defn render-topology
  [t]
  (let [graphs (:graphs t)
        all-edges (set (mapcat :edges graphs))]
    (with-out-str
      ;; External Topics and edges are outside of clusters
      (doseq [sub-graph graphs]
        (doseq [n (filter (partial ga/external-topic all-edges)
                          (:nodes sub-graph))]
          (println (render-el n)))
        (doseq [e (:edges sub-graph)]
          (println (render-edge e))))

      ;; Topology cluster
      (println (str "subgraph cluster_" (gviz-safe (:topology t)) " {"))
      (println "style=filled;")
      (println "fillcolor=gray90;")
      (println (str "label=\"" (gviz-safe (:topology t)) "\";"))

      ;; cluster per sub-topology / global store
      (doseq [sub-graph graphs]
        ;; List topics external to the sub-topology but internal to the graph
        (doseq [n (filter #(and
                            (ga/external-topic (:edges sub-graph) %)
                            (not (ga/external-topic all-edges %)))
                          (:nodes sub-graph))]
          (println (render-el n)))
        (println (str "subgraph cluster_" (gviz-safe (:id sub-graph)) " {"))
        (println "style=filled;")
        (println "fillcolor=gray80;")
        (println (str "label=\"" (gviz-safe (:name sub-graph)) "\";"))
        (doseq [n (filter #(and
                            (not (ga/external-topic (:edges sub-graph) %))
                            (not (ga/external-topic all-edges %)))
                          (:nodes sub-graph))]
          (println (render-el n)))
        (println "}"))
      ;; end subtopology clusters
      (println "}"))))

(defn ->digraph
  ([g] (->digraph g {}))
  ([g opts]
   (with-out-str
     (println "digraph G {")
     (println "rankdir=LR;")
     (doseq [[k v] opts]
       (println (str (name k) "=" (str v) ";")))
     (doseq [sg g]
       (println sg))
     (println "}"))))

(defn render
  [format dot out]
  ;; ensure that the directory exists already
  (io/make-parents (io/file out))
  (let [result (sh/sh "dot" "-T" format "-o" out :in dot)]
    (when-not (zero? (:exit result))
      (throw (Exception. (:err result))))))
