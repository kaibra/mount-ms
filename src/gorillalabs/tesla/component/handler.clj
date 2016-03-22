(ns gorillalabs.tesla.component.handler
  (:require [mount.core :as mnt]
            [clojure.tools.logging :as log]
            [ring.middleware.defaults :as ring-defaults]
            [ring.middleware.json :refer [wrap-json-response wrap-json-params wrap-json-body]]
            [ring.util.response :refer [content-type]]
            [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
            [buddy.auth :refer [authenticated? throw-unauthorized]]
            ))


(defmulti process (fn [handler _] handler))

(defn register [handler-component uri handler]
  (swap! handler-component assoc uri handler))

#_(defn- remove-route*
  [[route handler & routes] uri]
  (when route
    (if (= route uri)
      (recur routes uri)
      (conj (remove-route* routes uri) handler route))))

#_(defn remove-route
  [routes uri]
  (vec (remove-route* routes uri)))

(defn remove-route
    [routes uri]
    (dissoc routes uri))

(defn deregister [handler-component uri]
  (swap! handler-component remove-route uri))

(defn- start []
  (log/info "-> starting handler")
  (atom {}))

(defn- stop [handler]
  (log/info (str "<- stopping handler " handler)))


(declare handler)                                           ;; this is for Cursive IDE to pick up the symbol
(mnt/defstate ^{:on-reload :noop}
              handler
              :start (start)
              :stop (stop handler))

(defn- exception_caugth [& _] {:status 500 :body {:message "Internal error"}})

(defn- wrap-exception-handling
  "handles all not yet caught exceptions"
  [handler]
  (fn [request]
    (try
      (handler request)
      (catch Exception e
        (do (log/error (str "caught exception: " (.getMessage e)))
            (exception_caugth))))))

(defn- wrap-enforce-json-content-type
  "sets the content type to application/json, regardless to any existing value"
  [handler]
  (fn [request]
    (content-type (handler request) "application/json")))

(defn- wrap-block-not-authenticated-requests
  [handler]
  (fn [request]
    (if-not (authenticated? request)
      (throw-unauthorized)
      (handler request))))

(defn- wrap-common-api-handler
  [handler]
  (-> handler
      (wrap-exception-handling)
      (wrap-enforce-json-content-type)
      (ring-defaults/wrap-defaults ring-defaults/site-defaults)
      (wrap-json-body)
      (wrap-json-params)
      (wrap-json-response)
      ))

(defn wrap-secure-api [handler authorization]
  (-> handler
      (wrap-block-not-authenticated-requests)
      (wrap-authentication authorization)
      (wrap-authorization authorization)
      (wrap-common-api-handler)
      ))


(defn wrap-insecure-api [handler]
  (-> handler
      (wrap-common-api-handler)
      ))


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

