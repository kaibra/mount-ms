(ns tesla.system
  (:require [com.stuartsierra.component :as c]
            [tesla.component.serving :as serving]
            [tesla.component.app-status :as app-status]
            [tesla.component.health :as health]
            [tesla.component.configuring :as configuring]
            [tesla.component.metering :as metering]
            [tesla.component.keep-alive :as keep-alive]
            [beckon :as beckon]
            [clojure.tools.logging :as log]
            [environ.core :as env :only [env]]
            [tesla.component.handler :as handler]))

(defn wait! [system]
  (if-let [wait-time (get-in system [:config :config :wait-ms-on-stop])]
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
    :config (c/using (configuring/new-config runtime-config) [:keep-alive])
    :metering (c/using (metering/new-metering) [:config])
    :health (c/using (health/new-health) [:config :handler])
    :app-status (c/using (app-status/new-app-status) [:config :handler :metering])
    :server (c/using (serving/new-server) [:config :handler])))

;; deprecated stuff
(def empty-system base-system)
(def start-system start)
