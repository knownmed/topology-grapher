(ns topology-grapher.topics-test
  (:require [clojure.test :refer [deftest is testing]]
            [topology-grapher.sample-data :refer [sample-topology]]
            [topology-grapher.topics :as sut]))

(deftest topics-by-topology-test
  (testing "Grouping"
    (is (= {"topic-1" ["app"], "topic-2" ["app"]}
           (sut/topics-by-topology
            :inputs
            {"app" {:inputs #{"topic-1" "topic-2"}
                    :outputs #{"repayment-serviced-11"}}})))))

(deftest extract-topics-test
  (testing "Given a topology graph return the input and output topics"
    (is (= {:inputs #{}, :outputs #{}}
           (sut/extract-topics sample-topology)))))
