(ns topology-grapher.generator-test
  (:require [topology-grapher.generator :as sut]
            [clj-uuid]
            [topology-grapher.sample-data :refer [sample-topology]]
            [topology-grapher.sample-data :refer [gen-topology]]
            [clojure.test :refer [deftest testing is]]))

(def test-uuid "xxxxxxxxxxxxxxxx")

(deftest describe-topology-test
  (testing "A simple topology is described correctly"
    (with-redefs [clj-uuid/v5 (constantly test-uuid)]
      (is (= sample-topology (sut/describe-topology (gen-topology) "app.id"))))))
