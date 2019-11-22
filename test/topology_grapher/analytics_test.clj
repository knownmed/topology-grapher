(ns topology-grapher.analytics-test
  (:require [clojure.test :refer [deftest is]]
            [topology-grapher.analytics :as sut]))

(deftest type-test
  (is (sut/source? {:type :source}))
  (is (not (sut/source? {:type :sink}))))
