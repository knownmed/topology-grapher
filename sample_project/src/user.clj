(ns user
  (:require [hawk.core :as h]
            [describe :as d]))

(h/watch! [{:paths ["src"]
            :filter h/file?
            :handler (fn [ctx e]
                       (d/render-all!)
                       ctx)}])
