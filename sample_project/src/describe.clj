(ns describe
  (:gen-class)
  (:require [topology-grapher.describe :as td]
            [topology-grapher.render :as tr]
            [jackdaw-topology :as jt]
            [jackdaw.streams :as js]
            [interop-topology :as interop]))

(defn topology-from-stream-builder
  [stream-builder]
  (.build (js/streams-builder* stream-builder)))

(def meta-data
  {:domain "Big Corp"
   :subdomain "departement"
   :application "sample"})

(defn topologies
  []
  [{:topology (topology-from-stream-builder (jt/t1 (js/streams-builder)))
    :application-name "my-application-id"}

   {:topology (topology-from-stream-builder (jt/t2 (js/streams-builder)))
    :application-name "my-second-application-id"}

   {:topology (interop/gen-topology)
    :application-name "interop-topology"}])

(defn render-all!
  []
  (let [topologies (td/gen-topologies (topologies) meta-data)]
    (tr/render-graph (vals topologies) {:fmt "png" :mode "detail" :cache false})))
