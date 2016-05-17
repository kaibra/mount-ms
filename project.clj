(defproject gorillalabs/tesla "0.4.2"
  :description "basic microservice."
  :plugins [[lein-modules "0.3.11"]]
  :modules {:dirs       ["." "components/mongo" "components/titan" "components/quartzite"]
            :subprocess nil
            :inherited  {:url                 "https://github.com/gorillalabs/tesla"
                         :license             {:name "Apache License 2.0"
                                               :url  "http://www.apache.org/license/LICENSE-2.0.html"}
                         :deploy-repositories [["releases" :clojars]]
                         :scm                 {:dir ".."}
                         }
            :versions   {org.clojure/clojure "1.8.0"
                         org.clojure/tools.logging "0.3.1"
                         }
            }
  :dependencies [[org.clojure/clojure :version]
                 [mount "0.1.10"]
                 [gorillalabs/config "1.0.3"]
                 [org.clojure/data.json "0.2.6"]
                 [beckon "0.1.1"]
                 [environ "1.0.3"]
                 [clj-time "0.11.0"]


                 ;; Logging
                 [org.clojure/tools.logging :version]
                 [org.slf4j/slf4j-api "1.7.21"]
                 [org.slf4j/log4j-over-slf4j "1.7.21"]
                 [ch.qos.logback/logback-core "1.1.7"]
                 [ch.qos.logback/logback-classic "1.1.7"]

                 ;; HttpKit
                 [http-kit "2.1.19"]

                 ;; Authentication
                 [ring/ring-json "0.4.0"]
                 [buddy "0.13.0"]


                 ;; io
                 [ring/ring-core "1.4.0"]
                 [ring/ring-defaults "0.2.0"]
                 [bidi "2.0.9"]

                 ;; status
                 [de.otto/status "0.1.0"]

                 ;; metrics
                 [metrics-clojure "2.6.1"]
                 [metrics-clojure-graphite "2.6.1"]

]


  :exclusions [org.clojure/clojure
               org.slf4j/slf4j-nop
               org.slf4j/slf4j-log4j12
               log4j
               commons-logging/commons-logging]

  :test-paths ["test" "test-resources" "test-utils"]
  :test-selectors {:default     (constantly true)
                   :integration :integration
                   :unit        :unit
                   :all         (constantly true)}

  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
  ;; Releasing stuff
  ;;
  :scm {:name "git"
        :url  "https://github.com/gorillalabs/tesla"}

  :release-tasks [["vcs" "assert-committed"]
                  ["modules" "change" "version" "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag" "v"]
                  ["modules" "deploy"]
                  ["modules" "change" "version" "leiningen.release/bump-version"]
                  ["vcs" "commit"]
                  ["vcs" "push"]]


  :profiles {:uberjar {:aot :all}
             :dev     {:dependencies [[javax.servlet/servlet-api "2.5"]
                                      [org.clojure/tools.namespace "0.2.11"]
                                      [expectations "2.1.8"]
                                      [ring-mock "0.1.5"]]
                       :plugins      [[lein-pprint "1.1.1"]
                                      [lein-ancient "0.6.8"]
                                      [lein-marginalia "0.8.0"]
                                      [lein-environ "1.0.3"]
                                      [jonase/eastwood "0.2.3"]
                                      ]

                       :env          {:system "SYSTEM"
                                      :env    "ENV"}}}
  )
