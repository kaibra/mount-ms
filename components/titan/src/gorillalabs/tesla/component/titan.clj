(ns gorillalabs.tesla.component.titan
  (:require
   [clojurewerkz.titanium.graph :as tg]
   [clojure.tools.logging :as log]
   [gorillalabs.tesla.component.configuration :as config]
   [mount.core :as mnt]))

(defn titan-config [config]
  (merge {"storage.backend"  "cassandrathrift"
          "index.search.backend" "lucene"
          "index.search.directory" "/tmp/searchIndex"
          "attributes.custom.attribute1.attribute-class" "clojure.lang.Keyword"
          "attributes.custom.attribute1.serializer-class" "hamburg.cypp.KeywordSerializer"}
         config))

(defn- start []
  (log/info "-> starting titan")
  (let [server-config (titan-config (:titan config/configuration))
        graph (tg/open server-config)]
    graph))

(defn- stop [graph]
  (if graph
    (do
      (log/info "<- stopping titan")
      (tg/close graph))
    (log/info "<- titan not running")))


(mnt/defstate graph
              :start (start)
              :stop (stop graph))

