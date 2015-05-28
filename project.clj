(defproject gorillalabs/tesla-microservice "0.2.0-SNAPSHOT"
            :description "basic microservice, fork of https://github.com/otto-de/tesla-microservice"
            :url "https://github.com/otto-de/tesla-microservice"
            :license {:name "Eclipse Public License"
                      :url  "http://www.eclipse.org/legal/epl-v10.html"}
            :scm { :name "git"
      	           :url "https://github.com/gorillalabs/tesla-microservice"}
            :dependencies [[org.clojure/clojure "1.6.0"]
                           [com.stuartsierra/component "0.2.3"]
                           [org.clojure/data.json "0.2.6"]
                           [gorillalabs/config "1.0.0"]
                           [beckon "0.1.1"]
                           [overtone/at-at "1.2.0"]
                           [environ "1.0.0"]
                           [clj-time "0.9.0"]

                           [de.otto/status "0.1.0"]

                           ;; io
                           [ring/ring-core "1.3.2"]
                           [ring/ring-defaults "0.1.5"]

                           [compojure "1.3.4"]
                           [metrics-clojure "2.5.1"]
                           [metrics-clojure-graphite "2.5.1"]
                           [http-kit "2.1.18"]

                           ;; logging
                           [org.clojure/tools.logging "0.3.1"]
                           ]

            :profiles {:test {:env {:metering-reporter "console"
                                    :import-products   "false"
                                    :server-port       "9991"
                                    :cache-dir         "/tmp"}
                              :resource-paths ["test-resources"]
                              :dependencies [[ring-mock "0.1.5"]
                                             [http-kit.fake "0.2.2"]]
                              }
                       :dev {:dependencies [[javax.servlet/servlet-api "2.5"]]
                             :plugins [[lein-ancient "0.5.4"]
                                       [lein-marginalia "0.8.0"]
                                       [lein-environ "1.0.0"]]

                             }
                       :uberjar {:aot :all}})
