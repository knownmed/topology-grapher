(ns topology-grapher.zipit
  (:require [clojure.java.io :as io])
  (:import (java.util.zip ZipOutputStream ZipEntry ZipInputStream)))

(defmacro with-entry
  [zip entry-name & body]
  `(let [^ZipOutputStream zip# ~zip]
     (.putNextEntry zip# (ZipEntry. ~entry-name))
     ~@body
     (flush)
     (.closeEntry zip#)))

(defn zip-content
  "Zip all the given files into the desired output file"
  [output-filename edns]
  (println (format "zipping %d files into %s" (count edns) output-filename))
  (with-open [output (ZipOutputStream. (io/output-stream output-filename))]
    (doseq [[entry-name content] edns]
      (with-entry output entry-name
        (let [bs (.getBytes (str content))]
          (.write output bs 0 (count bs)))))))

(defn list-zip-file
  [zip-file size]
  (let [zip (ZipInputStream. (io/input-stream zip-file))]
    (for [_ (range size)]
      (.getName (.getNextEntry zip)))))

;; this ideally should not be needed at all
(def swap-file "/tmp/tmpfile.edn")

(defn load-content
  "Given a zip file return a map with file name and content"
  [zip-file]
  (with-open [input (ZipInputStream. (io/input-stream zip-file))]
    (loop [entry (.getNextEntry input)
           result {}]

      (if (nil? entry)
        result
        (let [output (io/output-stream swap-file)]
          (io/copy input output)
          (.close output)
          (recur (.getNextEntry input)
                 (assoc result
                        (.getName entry)
                        (-> swap-file slurp read-string))))))))
