(defproject gorillalabs/tesla-microservice "0.2.0-SNAPSHOT"
            :description "basic microservice, fork of https://github.com/otto-de/tesla-microservice"
            :url "https://github.com/otto-de/tesla-microservice"
            :license {:name "Eclipse Public License"
                      :url  "http://www.eclipse.org/legal/epl-v10.html"}
            :scm { :name "git"
      	           :url "https://github.com/gorillalabs/tesla-microservice"}
            :dependencies [[org.clojure/clojure "1.6.0"]
                           [com.stuartsierra/component "0.2.2"]
                           [org.clojure/core.cache "0.6.4"]
                           [clojurewerkz/propertied "1.2.0"]
                           [org.clojure/data.json "0.2.5"]
                           [beckon "0.1.1"]
                           [overtone/at-at "1.2.0"]
                           [environ "1.0.0"]
                           [clj-time "0.8.0"]

                           [de.otto/status "0.1.0"]

                           ;; io
                           [ring "1.3.1"]
                           [compojure "1.2.0"]
                           [hiccup "1.0.5"]
                           [metrics-clojure "2.3.0"]
                           [metrics-clojure-graphite "2.3.0"]

                           ;; logging
                           [org.clojure/tools.logging "0.3.0"]
                           ]

            :plugins [[lein-marginalia "0.8.0"]
                      [lein-environ "1.0.0"]]
            :profiles {:test {:env {:metering-reporter "console"
                                    :import-products   "false"
                                    :server-port       "9991"
                                    :cache-dir         "/tmp"}
                              :resource-paths ["test-resources"]
                              :dependencies [[ring-mock "0.1.5"]
                                             [http-kit.fake "0.2.1"]]
                              }
                       :uberjar {:aot :all}})
