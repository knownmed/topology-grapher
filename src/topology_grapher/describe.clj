(ns topology-grapher.describe
  (:require [topology-grapher.generator :as ggen]
            [topology-grapher.zipit :as zipit]
            [clojure.string :as s]
            [clojure.java.io :as io])
  (:import (org.apache.kafka.streams Topology)))

(defn- get-env-var
  [env-var]
  (System/getenv env-var))

(defn name-for-graph
  [graph]
  (format "%s.edn"
          (s/join "-"
                  (-> graph
                      (select-keys [:domain :subdomain :application :topology])
                      vals))))

;;TODO: would be nice to avoid this tight coupling with CircleCI
(defn git-sha [] (get-env-var "CIRCLE_SHA1"))
(defn git-branch [] (get-env-var "CIRCLE_BRANCH"))

(def base-path
  (or (get-env-var "ZIP_OUTPUT_DIR") "/tmp/zips"))

(defn zipfile-path
  [application-name]
  (format "%s/%s_%s.zip"
          base-path
          application-name
          (git-sha)))

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
  [topologies meta-data]
  (let [application-name (:application meta-data)
        graphs-by-name (gen-topologies topologies meta-data)
        zip-file-path (zipfile-path application-name)
        zip-file-master (format "%s/%s_%s.zip" base-path "latest" application-name)]

    (io/make-parents zip-file-path)
    (zipit/zip-content zip-file-path graphs-by-name)
    (when (= "master" (git-branch))
      (zipit/zip-content zip-file-master graphs-by-name))))
