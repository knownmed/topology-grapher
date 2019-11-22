(ns topology-grapher.describe
  (:require [topology-grapher.generator :as ggen]
            [topology-grapher.zipit :as zipit]
            [clojure.string :as s]
            [clojure.java.io :as io])
  (:import (org.apache.kafka.streams StreamsBuilder Topology)))

(defn- get-env-var
  [env-var]
  (System/getenv env-var))

(defn name-for-graph [graph]
  (format "%s.edn"
          (s/join "-" (vals (select-keys graph [:domain
                                                :subdomain
                                                :application
                                                :topology])))))

;;TODO: would be nice to avoid this tight coupling with CircleCI
(defn git-sha [] (get-env-var "CIRCLE_SHA1"))
(defn git-branch [] (get-env-var "CIRCLE_BRANCH"))

(def base-path
  (or (get-env-var "ZIP_OUTPUT_DIR") "/tmp/zips"))

(defn app-id
  [config]
  (get config "application.id"))

(defn topology->graph
  [topology config meta-data]
  (merge
   meta-data
   (ggen/describe-topology
    (let [g (topology (StreamsBuilder.))]
      ;; this could be avoided if we enforce a single type
      (if (instance? Topology g)
        g
        (.build g)))

    (app-id config))))

(defn zipfile-path
  [application-name]
  (format "%s/%s_%s.zip"
          base-path
          application-name
          (git-sha)))

(defn describe-all
  "Describe all the topologies"
  [topologies meta-data]
  (let [application-name (:application meta-data)
        graphs (map (fn [tc]
                      (topology->graph (:fn tc) (:config tc) meta-data)) topologies)
        graphs-by-name (into {}
                             (for [g graphs]
                               {(name-for-graph g) g}))
        zip-file-path (zipfile-path application-name)
        zip-file-master (format "%s/%s_%s.zip" base-path "latest" application-name)]

    (io/make-parents zip-file-path)
    (zipit/zip-content zip-file-path graphs-by-name)
    (when (= "master" (git-branch))
      (zipit/zip-content zip-file-master graphs-by-name))))
