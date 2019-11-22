(ns topology-grapher.analytics)

(defn source? [s]
  (= :source (:type s)))

(defn sink? [s]
  (= :sink (:type s)))

(defn processor? [s]
  (= :processor (:type s)))

(defn topic? [s]
  (= :topic (:type s)))

(defn store? [s]
  (= :store (:type s)))

(defn input-topic [edges n]
  ;; its a topic, and its at the start of the graph, i.e. there
  ;; is no edge going to it
  (and (topic? n)
       (not-any? #(= (:id n) (:to-id %)) edges)))

(defn output-topic [edges n]
  ;; its a topic, and its at the end of the graph, i.e. there
  ;; is no edge coming from it
  (and (topic? n)
       (not-any? #(= (:id n) (:from-id %)) edges)))

(defn external-topic [edges n]
  (or (input-topic edges n)
      (output-topic edges n)))

(defn prune-to-topology [g]
  (let [graphs (:graphs g)
        all-edges (set (mapcat :edges graphs))
        in-t (filter #(input-topic all-edges %) (mapcat :nodes graphs))
        out-t (filter #(output-topic all-edges %) (mapcat :nodes graphs))
        topology-node {:type :topology
                       :name (:topology g)
                       :id (:id g)}]
    (assoc g :graphs [{:type :stream
                       :name (:topology g)
                       :id (:id g)
                       :nodes (concat in-t out-t [topology-node])
                       :edges (concat (map (fn [n]
                                             {:from (:name n)
                                              :to (:name topology-node)
                                              :from-id (:id n)
                                              :to-id (:id topology-node)}) in-t)
                                      (map (fn [n]
                                             {:from (:name topology-node)
                                              :to (:name n)
                                              :from-id (:id topology-node)
                                              :to-id (:id n)}) out-t))}])))
