(ns topology-grapher.describe
  (:require [topology-grapher.generator :as ggen]
            [topology-grapher.zipit :as zipit]
            [clojure.string :as s]
            [clojure.java.io :as io])
  (:import (org.apache.kafka.streams Topology)))

(def fields [:domain :subdomain :application :topology])

(defn name-for-graph
  "Generate a human readable name for the topology"
  [graph]
  (format "%s.edn"
          (s/join "-"
                  (-> graph
                      (select-keys [:domain :subdomain :application :topology])
                      vals))))

(def default-zip-path "/tmp/zips")

(defn zipfile-path
  [application-name base-path sha]
  (if (some? sha)
    (format "%s/%s_%s.zip"
            base-path
            application-name
            (sha))
    (format "%s_%s.zip" base-path application-name)))

(defn gen-topologies
  [topologies meta-data]
  (->> topologies
       (map (fn [{:keys [^Topology topology application-name]}]
              (merge meta-data
                     (ggen/describe-topology
                      topology
                      application-name))))
       (map (fn [g] {(name-for-graph g) g}))
       (into {})))

(defn generate-zip
  "Describe all the topologies and create a zip file with the result"
  ([topologies meta-data]
   (generate-zip topologies meta-data default-zip-path {}))
  ([topologies meta-data base-path {:keys [sha branch] :as rev}]
   (let [application-name (:application meta-data)
         graphs-by-name (gen-topologies topologies meta-data)
         zip-file-path (zipfile-path base-path application-name sha)
         zip-file-master (format "%s/%s_%s.zip" base-path "latest" application-name)]

     (io/make-parents zip-file-path)
     (zipit/zip-content zip-file-path graphs-by-name)
     (when (= "master" branch)
       (zipit/zip-content zip-file-master graphs-by-name)))))
