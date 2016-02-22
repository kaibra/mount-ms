(ns kaibra.stateful.health
  (:require [compojure.core :as c]
            [clojure.tools.logging :as log]
            [mount.core :refer [defstate]]))

(defn start-health []
  (log/info "-> Starting healthcheck.")
  {:locked (atom false)})

(defstate health :start (start-health))
