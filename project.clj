(defproject fundingcircle/topology-grapher "_"
  :description
  "tool for graphing kafka topologies"
  :license {:name "BSD 3-clause"
            :url "http://opensource.org/licenses/BSD-3-Clause"}
  :dependencies [[org.clojure/clojure "1.10.0" :scope "provided"]
                 [danlentz/clj-uuid "0.1.7"]
                 [digest "1.4.9"]
                 ;; this library should be always available given the types
                 ;; of project that are using this library
                 [org.apache.kafka/kafka-streams "2.2.0" :scope "provided"]
                 [org.clojure/tools.cli "0.4.2"]]

  :plugins [[me.arrdem/lein-git-version "2.0.8"]
            [jonase/eastwood "0.3.5"]]

  :aliases {"kaocha" ["run" "-m" "kaocha.runner"]
            "tag" ["run" "-m" "grafana-helpers.release"]}

  :test-paths ["test"]
  :main topology-grapher.core
  :git-version
  {:status-to-version
   (fn [{:keys [tag branch ahead? dirty?] :as git}]
     (if (and tag (not ahead?) (not dirty?))
       tag
       (let [[_ prefix patch] (re-find #"(\d+\.\d+)\.(\d+)" tag)
             patch            (Long/parseLong patch)
             patch+           (inc patch)]
         (format "%s.%d-%s-SNAPSHOT" prefix patch+ branch))))}

  :cljfmt {:indents {for-all [[:block 1]]
                     fdef [[:block 1]]
                     checking [[:inner 0]]}}
  :profiles
  {:dev {:dependencies [[andreacrotti/semver "0.2.1"]
                        [lambdaisland/kaocha "0.0-389"]
                        [lambdaisland/kaocha-cloverage "0.0-22"]
                        [lambdaisland/kaocha-junit-xml "0.0-63"]]
         :plugins [[jonase/eastwood "0.3.5"]
                   [lein-cljfmt "0.5.7"]]}}

  :repositories
  {"confluent" {:url "https://packages.confluent.io/maven/"}})
