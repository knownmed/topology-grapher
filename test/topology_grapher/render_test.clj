(ns topology-grapher.render-test
  (:require [topology-grapher.render :as sut]
            [topology-grapher.sample-data :as sample]
            [clojure.java.io :as io]
            [clojure.test :refer [deftest testing is]]))

;; different options to test
(def opts-matrix
  (for [f sut/fmts, m sut/modes, c [true false]]
    [f m c]))

(deftest render-graph-test
  (testing "render simple graph"
    (let [output-file (sut/render-graph [sample/sample-topology])
          ff (io/file output-file)]
      (is (.exists ff))
      (is (pos? (.length ff)))))

  (testing "try all possible options"
    (doseq [[fmt mode cache] opts-matrix]
      (let [output-file (sut/render-graph [sample/sample-topology]
                                          {:fmt fmt :mode mode :cache cache})
            ff (io/file output-file)]
        (is (.exists ff))
        (is (pos? (.length ff)))))))
