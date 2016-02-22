(ns kaibra.stateful.app-status
  (:require [clojure.tools.logging :as log]
            [kaibra.transition.configuring :as conf]
            [de.otto.status :as s]
            [mount.core :refer [defstate]]))

(defn- aggregation-strategy []
  (if (= (conf/conf-prop :status-aggregation) "forgiving")
    s/forgiving-strategy
    s/strict-strategy))

(defn- start-app-status []
  (log/info "-> start app-status")
  {:status-aggregation (aggregation-strategy)
   :status-functions   (atom [])})

(defstate app-status :start (start-app-status))

