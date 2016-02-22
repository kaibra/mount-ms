(ns kaibra.transition.health
  (:require [compojure.core :as c]
            [kaibra.stateful.health :refer [health]]
            [mount.core :refer [defstate]]
            [kaibra.transition.configuring :as conf]))

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
