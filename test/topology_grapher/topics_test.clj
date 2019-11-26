(ns topology-grapher.topics-test
  (:require [clojure.test :refer [deftest is testing]]
            [topology-grapher.topics :as sut]))

(deftest ^:integration topics-by-topology-test
  (testing "Grouping"
    (is (= {"topic-1" ["app"], "topic-2" ["app"]}
           (sut/topics-by-topology
            :inputs
            {"app" {:inputs #{"topic-1" "topic-2"}
                    :outputs #{"repayment-serviced-11"}}})))))
