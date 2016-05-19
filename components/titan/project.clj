(defproject gorillalabs.tesla/titan "0.4.8-SNAPSHOT"
            :plugins [[lein-modules "0.3.11"]]
            :modules {:parent "../.."}
            :description "A titan component"
            :dependencies [[gorillalabs.tesla/core :version]
                           [gorillalabs/titanium "1.0.0-beta3-SNAPSHOT"]
                           [com.thinkaurelius.titan/titan-cassandra "1.0.0"]
                           [com.thinkaurelius.titan/titan-lucene "1.0.0"]
                           [org.xerial.snappy/snappy-java "1.1.2.4"] ;; keep this version
                           [commons-codec "1.10"]  ;; keep this version
                           [commons-lang "2.6"]  ;; keep this version
                           ])
