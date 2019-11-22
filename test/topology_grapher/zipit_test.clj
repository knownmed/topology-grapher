(ns topology-grapher.zipit-test
  (:require [topology-grapher.zipit :as sut]
            [clojure.test :refer [deftest testing is]]))

(def content
  {"file1" "hello"
   "file2" "world"})

(def zip-file "test.zip")

(deftest zip-test
  (testing "Zipping and reloading works"
    (sut/zip-content zip-file content)
    (is (= (keys content) (sut/list-zip-file zip-file (count content))))
    (is (= (keys content) (keys (sut/load-content zip-file))))))
