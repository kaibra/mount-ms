(ns gorillalabs.tesla.component.httpkit
  (:require [bidi.ring :as br]
            [org.httpkit.server :as httpkit]
            [clojure.tools.logging :as log]
            [gorillalabs.tesla.component.configuration :as config]
            [gorillalabs.tesla.component.handler :as handler]
            [mount.core :as mnt]))


(def default-port 3000)

(defn parser-string-config [config element default-value]
  (get-in config [:config element] default-value))

(defn parser-integer-config [config element default-value]
  (try
    (Integer. (parser-string-config config element default-value))
    (catch NumberFormatException e default-value)))

(defn server-config [config]
  {:port       (parser-integer-config config :server-port default-port)
   :ip         (parser-string-config config :server-bind "0.0.0.0")
   :thread     (parser-integer-config config :server-thread 4)
   :queue-size (parser-integer-config config :server-queue-size 20000)
   :max-body   (parser-integer-config config :server-max-body 8388608)
   :max-line   (parser-integer-config config :server-max-line 4096)
   })



(defn- start []
  (log/info "-> starting httpkit")
  (let [server-config (server-config config/configuration)
        routes ["/" @handler/handler]
        _ (log/info "Starting httpkit with port " (server-config :port) " and bind " (server-config :ip) ".")
        server (httpkit/run-server (bidi.ring/make-handler routes) server-config)]
    server))

(defn- stop [server]
  (let [timeout (config/config config/configuration [:httpkit-timeout] 100)]
    (if server
      (do
        (log/info "<- stopping httpkit with timeout:" timeout "ms")
        (server :timeout timeout))
      (log/info "<- stopping httpkit"))))


(mnt/defstate httpkit
              :start (start)
              :stop (stop httpkit))
