(ns topology-grapher.describe-test
  (:require [topology-grapher.describe :as sut]
            [topology-grapher.sample-data :as t]
            [topology-grapher.zipit :refer [list-zip-file]]
            [clojure.java.io :as io]
            [clojure.test :refer [deftest testing is use-fixtures]]))

(def sample-sha "git-sha")
(def output-zip-file (format "/tmp/zips/test-application_%s.zip" sample-sha))
(def output-zip-latest "/tmp/zips/latest_test-application.zip")

(defn- delete-test-zip
  []
  (try
    (io/delete-file output-zip-file)
    (catch java.io.IOException e)))

(use-fixtures :each
  (fn [t]
    (delete-test-zip)
    (with-redefs [sut/zipfile-path (constantly output-zip-file)]
      (t))
    (delete-test-zip)))

(def meta-data {:application "application" :domain "domain"})

(deftest generate-zip-test
  (testing "generating one zip file for a branch different from master"
    (sut/generate-zip t/topologies t/meta-data)
    (is (true? (.isFile (io/file output-zip-file))))
    (is (= #{"world-test-application-test-application.edn"}
           (set (list-zip-file output-zip-file 1)))))

  (testing "generating two zip files for master"
    (with-redefs [sut/git-branch (constantly "master")]
      (sut/generate-zip t/topologies t/meta-data))

    (is (true? (.isFile (io/file output-zip-file))))
    (is (true? (.isFile (io/file output-zip-latest))))))

(deftest name-for-graph-test
  (is (= "domain-subdomain-application-topology.edn"
         (sut/name-for-graph {:domain "domain"
                              :subdomain "subdomain"
                              :application "application"
                              :topology "topology"}))))
