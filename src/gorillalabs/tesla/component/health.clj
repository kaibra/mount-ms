(ns gorillalabs.tesla.component.health
  (:require [mount.core :as mnt]
            [compojure.core :as c]
            [clojure.tools.logging :as log]
            [gorillalabs.tesla.stateful.handler :as handler]
            [ring.middleware.defaults :as ring-defaults]
            [gorillalabs.tesla.stateful.configuration :as config]))

;; http response for a healthy system
(def healthy-response {:status  200
                       :headers {"Content-Type" "text/plain"}
                       :body    "HEALTHY"})
;; http response for an unhealthy system
(def unhealthy-response {:status  503
                         :headers {"Content-Type" "text/plain"}
                         :body    "UNHEALTHY"})

(defn- health-response [self]
  (if @(:locked self)
    unhealthy-response
    healthy-response))

(defn make-handler
  [self]
  (let [health-path (config/config (:config self) [:health :path] "/health")]
    (c/routes (c/GET health-path
                     []
                (-> (c/GET health-path
                           []
                      (health-response self))
                    (ring-defaults/wrap-defaults
                      (assoc ring-defaults/site-defaults :session false
                                                         :cookies false
                                                         :static false
                                                         :proxy true)))))))

(defn lock [health]
  (reset! (:locked self) true))

(defn unlock [health]
  (reset! (:locked self) false))


(defn- start []
  (log/info "-> Starting healthcheck.")
  (let [new-self (atom false)]
    (handler/register-handler handler (make-handler new-self))
    new-self))

(defn- stop [self]
  (log/info "<- Stopping Healthcheck")
  self)

(mnt/defstate health
              :start (start)
              :stop (stop health))



