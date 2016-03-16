(ns gorillalabs.tesla.component.handler
  (:require [mount.core :as mnt]
            [clojure.tools.logging :as log]
            [ring.middleware.defaults :as ring-defaults]))


(defmulti process (fn [handler _] handler))

(defn register [handler-component uri handler]
  (swap! handler-component conj uri handler))

(defn- remove-route*
  [[route handler & routes] uri]
  (when route
    (if (= route uri)
      (recur routes uri)
      (conj (remove-route* routes uri) handler route))))

(defn remove-route
  [routes uri]
  (vec (remove-route* routes uri)))

(defn deregister [handler-component uri]
  (swap! handler-component remove-route uri)
  )

(defn- start []
  (log/info "-> starting handler")
  (atom []))

(defn- stop []
  (log/info "<- stopping handler")
  )

(mnt/defstate handler
              :start (start)
              :stop (stop))

(defn wrap-api [handler]
  (ring-defaults/wrap-defaults
    handler
    (assoc ring-defaults/secure-api-defaults
      :static false
      :proxy true)))


(defn wrap-site [handler]
  (ring-defaults/wrap-defaults
    handler
    (-> ring-defaults/secure-site-defaults
        (assoc
          :session false
          :cookies false
          :static false
          :proxy true)
        (assoc-in [:security :hsts] false)
        (assoc-in [:security :ssl-redirect] false)
        )))

