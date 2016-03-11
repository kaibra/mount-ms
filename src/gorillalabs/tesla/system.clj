(ns gorillalabs.tesla.system
  (:require [com.stuartsierra.component :as c]
            [gorillalabs.tesla.stateful.app-status :as app-status]
            [gorillalabs.tesla.stateful.health :as health]
            [gorillalabs.tesla.stateful.configuring :as config]
            [gorillalabs.tesla.stateful.metering :as metering]
            [gorillalabs.tesla.stateful.keep-alive :as keep-alive]
            [beckon :as beckon]
            [clojure.tools.logging :as log]
            [environ.core :as env :only [env]]
            [gorillalabs.tesla.stateful.handler :as handler]
            ))

(defn wait! [system]
  (if-let [wait-time (config/config (:config system) [:wait-ms-on-stop])]
    (try
      (log/info "<- Waiting " wait-time " milliseconds.")
      (Thread/sleep (Integer. wait-time))
      (catch Exception e (log/error e)))))

(defn stop [system]
  (beckon/reinit-all!)
  (log/info "<- System will be stopped. Setting lock.")
  (health/lock-application (:health system))
  (wait! system)
  (log/info "<- Stopping system.")
  (c/stop system))

(defn start [system]
  (let [started (c/start system)]
    (doseq [sig ["INT" "TERM"]]
      (reset! (beckon/signal-atom sig) #{(partial stop started)}))
    started))

(defn base-system [runtime-config]
  (c/system-map
    :keep-alive (keep-alive/new-keep-alive)
    :handler (handler/new-handler)
    :config (c/using (config/new-config runtime-config) [:keep-alive])
    :metering (c/using (metering/new-metering) [:config])
    :health (c/using (health/new-health) [:config :handler])
    :app-status (c/using (app-status/new-app-status) [:config :handler :metering])))
