(ns tesla.component.serving
  (:require [com.stuartsierra.component :as component]
            [org.httpkit.server :refer [run-server]]
            [tesla.component.routes :as rts]
            [compojure.handler :refer [site]]
            [clojure.tools.logging :as log])
  (:import [clojure.lang RT]))


(defn
  assoc-if-not-nil
  "assoc[iate]. When applied to a map, returns a new map of the
      same (hashed/sorted) type, that contains the mapping of key(s) to
      val(s) if val(s) are not nil. When applied to a vector, returns a new vector that
      contains val at index. Note - index must be <= (count vector)."

  ([map key val] (if-not (nil? val)
                   (. RT (assoc map key val))
                   map))
  ([map key val & kvs]
   (let [ret (assoc-if-not-nil map key val)]
     (if kvs
       (if (next kvs)
         (recur ret (first kvs) (second kvs) (nnext kvs))
         (throw (IllegalArgumentException.
                  "assoc-if-not-nil expects even number of arguments after map/vector, found odd number")))
       ret))))


(def default-port 3000)

;; The serving component is the frontend of the system.
;; It accepts requests and returns the data to be used by consuming systems.
;; For the moment a simple, blocking implementation with an embedded jetty is chosen.
(defrecord Server [config routes]
  component/Lifecycle
  (start [self]
    (log/info "-> starting server")
    (let [port (get-in config [:config :server :port] default-port)
          all-routes (rts/routes routes)
          server (run-server (site all-routes)
                             (assoc-if-not-nil
                               {:port port}
                               :ip (get-in config [:config :server :bind])
                               ))]
      (assoc self :server server)))

  (stop [self]
    (log/info "<- stopping server")
    (when-let [server (:server self)]
      (server))
    self))

(defn new-server [] (map->Server {}))
