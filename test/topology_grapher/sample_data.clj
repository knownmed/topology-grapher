(ns topology-grapher.sample-data)

(def sample-topology
  {:topology "app.id",
   :id "xxxxxxxxxxxxxxxx",
   :graphs
   [{:type :stream,
     :name "stream-0",
     :nodes
     [{:type :processor,
       :name "KSTREAM-MAPVALUES-0000000002",
       :id "xxxxxxxxxxxxxxxx"}
      {:type :source,
       :name "KSTREAM-SOURCE-0000000000",
       :id "xxxxxxxxxxxxxxxx"}
      {:type :sink,
       :name "KSTREAM-SINK-0000000003",
       :id "xxxxxxxxxxxxxxxx"}
      {:type :topic, :name "stream1-topic", :id "xxxxxxxxxxxxxxxx"}
      {:type :topic, :name "topic-1", :id "xxxxxxxxxxxxxxxx"}
      {:type :processor,
       :name "KSTREAM-FILTER-0000000001",
       :id "xxxxxxxxxxxxxxxx"}],
     :edges [],
     :id "xxxxxxxxxxxxxxxx"}
    {:type :stream,
     :name "stream-1",
     :nodes
     [{:type :sink,
       :name "KSTREAM-SINK-0000000005",
       :id "xxxxxxxxxxxxxxxx"}
      {:type :topic, :name "stream2-topic", :id "xxxxxxxxxxxxxxxx"}
      {:type :source,
       :name "KSTREAM-SOURCE-0000000004",
       :id "xxxxxxxxxxxxxxxx"}
      {:type :topic, :name "topic-2", :id "xxxxxxxxxxxxxxxx"}],
     :edges [],
     :id "xxxxxxxxxxxxxxxx"}]})

(def sample-graphviz
  "xxxxxxxxxxxxxxxx [label=stream1_topic;shape=cds;style=filled;fillcolor=green2];
xxxxxxxxxxxxxxxx [label=topic_1;shape=cds;style=filled;fillcolor=green2];
xxxxxxxxxxxxxxxx [label=stream2_topic;shape=cds;style=filled;fillcolor=green2];
xxxxxxxxxxxxxxxx [label=topic_2;shape=cds;style=filled;fillcolor=green2];
subgraph cluster_app_id {
style=filled;
fillcolor=gray90;
label=\"app_id\";
subgraph cluster_xxxxxxxxxxxxxxxx {
style=filled;
fillcolor=gray80;
label=\"stream_0\";
xxxxxxxxxxxxxxxx [label=MAPVALUES;shape=parallelogram;style=filled;fillcolor=lightblue];
xxxxxxxxxxxxxxxx [label=SOURCE;shape=box;style=filled;fillcolor=gray70];
xxxxxxxxxxxxxxxx [label=SINK;shape=box;style=filled;fillcolor=gray70];
xxxxxxxxxxxxxxxx [label=FILTER;shape=parallelogram;style=filled;fillcolor=lightblue];
}
subgraph cluster_xxxxxxxxxxxxxxxx {
style=filled;
fillcolor=gray80;
label=\"stream_1\";
xxxxxxxxxxxxxxxx [label=SINK;shape=box;style=filled;fillcolor=gray70];
xxxxxxxxxxxxxxxx [label=SOURCE;shape=box;style=filled;fillcolor=gray70];
}\n}\n")
