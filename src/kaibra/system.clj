(ns kaibra.system
  (:require
    [beckon :as beckon]
    [clojure.tools.logging :as log]
    [mount.core :as mnt]
    [kaibra.stateful.health :as health]
    [kaibra.stateful.configuring :as config]
    [kaibra.stateful.metering :as metering]
    [kaibra.stateful.keep-alive :as keep-alive]
    [kaibra.stateful.app-status :as app-status]))

(def states
  [#'metering/metering
   #'keep-alive/keep-alive
   #'app-status/app-status
   #'config/config
   #'health/health])

(defn wait! []
  (if-let [^String wait-time (config/conf-prop :wait-ms-on-stop)]
    (try
      (log/info "<- Waiting " wait-time " milliseconds.")
      (Thread/sleep (Integer. wait-time))
      (catch Exception e
        (log/error e)))))

(defn stop [custom-states]
  (beckon/reinit-all!)
  (log/info "<- System will be stopped. Setting lock.")
  (health/lock-application)
  (wait!)
  (log/info "<- Stopping system.")
  (apply mnt/stop (concat states custom-states)))

(defn start [& custom-states]
  (log/info "-> Starting the MOUNT-MS base system")
  (apply mnt/start (concat states custom-states))
  (doseq [sig ["INT" "TERM"]]
    (reset! (beckon/signal-atom sig) #{(partial stop custom-states)})))
