(ns kaibra.stateful.health
  (:require [compojure.core :as c]
            [clojure.tools.logging :as log]
            [mount.core :refer [defstate]]
            [kaibra.stateful.configuring :as conf]))

(defn start-health []
  (log/info "-> Starting healthcheck.")
  {:locked (atom false)})

(defstate health :start (start-health))

;; http response for a healthy system
(def healthy-response {:status  200
                       :headers {"Content-Type" "text/plain"}
                       :body    "HEALTHY"})
;; http response for an unhealthy system
(def unhealthy-response {:status  503
                         :headers {"Content-Type" "text/plain"}
                         :body    "UNHEALTHY"})

(defn health-response [self]
  (if @(:locked self)
    unhealthy-response
    healthy-response))

(defn health-handler []
  (let [health-path (or (conf/conf-prop :health-url) "/health")]
    (c/routes (c/GET health-path [] (health-response health)))))

(defn lock-application []
  (reset! (:locked health) true))
