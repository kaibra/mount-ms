(ns gorillalabs.tesla.component.health
  (:require [mount.core :as mnt]
            [bidi.bidi :as bidi]
            [clojure.tools.logging :as log]
            [gorillalabs.tesla.component.configuration :as config]
            [gorillalabs.tesla.component.handler :as handler]
            [ring.middleware.defaults :as ring-defaults]
            ))


(declare health)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; http related stuff

;; http response for a healthy system
(def healthy-response {:status  200
                       :headers {"Content-Type" "text/plain"}
                       :body    "HEALTHY"})
;; http response for an unhealthy system
(def unhealthy-response {:status  503
                         :headers {"Content-Type" "text/plain"}
                         :body    "UNHEALTHY"})

(defn- health-response [healthy?]
  (if @healthy?
    healthy-response
    unhealthy-response))



(defn- handle [request]
  (health-response health))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; lifecycle functions

(defn- start []
  (log/info "-> starting healthcheck.")
  (let [healthy? (atom true)]
    (handler/register
        handler/handler
        (config/config config/configuration [:health :path] "/health")
        (handler/wrap-site #'handle))
    healthy?))

(defn- stop [self]
  (log/info "<- stopping Healthcheck")
  (handler/deregister
    handler/handler
    (config/config config/configuration [:health :path] "/health"))
  self)

(mnt/defstate health
              :start (start)
              :stop (stop health))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; API to this component

(defn lock [health]
  (reset! health true))

(defn unlock [health]
  (reset! health false))





