(ns topology-grapher.render
  (:require [digest :refer [md5]]
            [clojure.java.io :as io]
            [clojure.string :as s]
            [clojure.edn :as edn]
            [topology-grapher.analytics :as a :refer [prune-to-topology]]
            [topology-grapher.gviz :as gviz]
            [topology-grapher.config :as c]))

(def fmts #{"dot" "png" "pdf"})
(def modes #{"detail" "topics"})
(def caches boolean?)

(defn load-edn
  [path]
  (-> path slurp edn/read-string))

(defn render-all
  [gl renderer wrapper]
  (wrapper (map renderer gl)))

(defn- combined-graph-dot
  [topologies mode]
  (assert (seq topologies) "Empty topology list")
  (render-all
   (if (= mode "detail")
     topologies
     (map prune-to-topology topologies))

   gviz/render-topology
   (if (= mode "detail")
     gviz/->digraph
     (fn [g]
       (gviz/->digraph g {:clusterrank "none"})))))

(defn render-graph
  "Render the given topologies out to file"
  ([topologies]
   (render-graph topologies {:cache true :fmt "png" :mode "detail"}))
  ([topologies {:keys [fmt mode output-file cache base-dir]}]
   {:pre [(modes mode) (fmts fmt) (caches cache)]}
   (let [ids (map :id topologies)
         base-directory (or base-dir c/base-directory)
         ids-hash (md5 (s/join ids))
         filename (or output-file
                      (format "%s/%s_%s.%s" base-directory mode ids-hash fmt))
         combined-graph (combined-graph-dot topologies mode)]
     (if (= fmt "dot")
       (spit filename combined-graph)
       (when-not (and cache (.exists (io/file filename)))
         (gviz/render fmt combined-graph filename)))

     filename)))
