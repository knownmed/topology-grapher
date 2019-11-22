(ns topology-grapher.gviz-test
  (:require [clojure.test :refer [deftest is]]
            [topology-grapher.sample-data :as sample]
            [topology-grapher.gviz :as sut]))

(deftest gviz-safe-test
  (is (= "safe_already" (sut/gviz-safe "safe_already")))
  (is (= "no_spaces" (sut/gviz-safe "no spaces")))
  (is (= "other_seps_disallowed" (sut/gviz-safe "other-seps.disallowed")))
  (is (= "n1nonumatstart" (sut/gviz-safe "1nonumatstart"))))

(deftest render-topology-test
  (is (= sample/sample-graphviz (sut/render-topology sample/sample-topology))))
