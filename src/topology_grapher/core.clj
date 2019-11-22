(ns topology-grapher.core
  (:gen-class)
  (:require [topology-grapher.render :as graphs]
            [clojure.tools.cli :as cli]))

(def options-specs
  [["-i" "--input INPUT" "edn file to render"]
   ["-o" "--output OUTPUT" "where to write to"]
   ["-m" "--mode MODE" "detail or topics"]])

(defn -main
  [& args]
  (let [options (cli/parse-opts args options-specs)
        loaded (graphs/load-edn (-> options :options :input))
        output-file (graphs/render-graph [loaded]
                                         {:format "png"
                                          :mode "detail"
                                          :cache false
                                          :output-file (-> options :options :output)})]

    (println "Written graph to " output-file)))
