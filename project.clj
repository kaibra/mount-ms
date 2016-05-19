(defproject gorillalabs/tesla "0.4.6"
  :description "basic microservice."
  :plugins [[lein-modules "0.3.11"]]
  :modules {:dirs       ["core/" "components/mongo" "components/titan/" "components/quartzite/"]
            :subprocess nil
            :inherited  {:url                 "https://github.com/gorillalabs/tesla"
                         :license             {:name "Apache License 2.0"
                                               :url  "http://www.apache.org/license/LICENSE-2.0.html"}
                         :deploy-repositories [["releases" :clojars]]
                         :scm                 {:dir ".."}
                         :exclusions [org.clojure/clojure
                                      org.slf4j/slf4j-nop
                                      org.slf4j/slf4j-log4j12
                                      log4j
                                      commons-logging/commons-logging]
                         }
            :versions   {org.clojure/clojure       "1.8.0"
                         org.clojure/tools.logging "0.3.1"
                         }
            }

  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
  ;; Releasing stuff
  ;;
  :scm {:name "git"
        :url  "https://github.com/gorillalabs/tesla"}

  :profiles {:provided
             {:dependencies [[org.clojure/clojure "_"]]}}

  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["modules" "change" "version" "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag" "v"]
                  ["modules" "deploy"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["modules" "change" "version" "leiningen.release/bump-version"]
                  ["vcs" "commit"]
                  ["vcs" "push"]
                  ]
  )
