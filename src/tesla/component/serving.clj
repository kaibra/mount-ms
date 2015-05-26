(ns tesla.component.serving
  (:require [com.stuartsierra.component :as component]
            [org.httpkit.server :refer [run-server]]
            [tesla.component.routes :as rts]
            [compojure.handler :refer [site]]
            [clojure.tools.logging :as log]))

;; The serving component is the frontend of the system.
;; It accepts requests and returns the data to be used by consuming systems.
;; For the moment a simple, blocking implementation with an embedded jetty is chosen.
(defrecord Server [config routes]
  component/Lifecycle
  (start [self]
    (log/info "-> starting server")
    (let [port (Integer. (get-in config [:config :server :port]))
          all-routes (rts/routes routes)
          server (run-server (site all-routes) {:ip (get-in config [:config :server :bind]) :port port})]
      (assoc self :server server)))

  (stop [self]
    (log/info "<- stopping server")
    (when-let [server (:server self)]
      (server))
    self))

(defn new-server [] (map->Server {}))
