(ns tesla.component.health
  (:require [com.stuartsierra.component :as component]
            [compojure.core :as c]
            [clojure.tools.logging :as log]
            [tesla.component.handler :as handler]
            [ring.middleware.defaults :as ring-defaults]))

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

(defn make-handler
  [self]
  (c/routes (c/GET
              (get-in self [:config :config :health :path] "/health")
              []
              (->
                  (health-response self)
                  (ring-defaults/wrap-defaults
                    (assoc ring-defaults/site-defaults :session false
                                                       :cookies false
                                                       :proxy true))))))

(defn lock-application [self]
  (reset! (:locked self) true))



(defrecord Health [config handler]
  component/Lifecycle
  (start [self]
    (log/info "-> Starting healthcheck.")
    (let [new-self (assoc self :locked (atom false))]
      (handler/register-handler handler (make-handler new-self)) ;; TODO: use config directly
      new-self))

  (stop [self]
    (log/info "<- Stopping Healthcheck")
    self))

(defn new-health []
  (map->Health {}))

