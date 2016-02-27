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

(def the-states
  {:app-status #'app-status/app-status
   :config     #'config/config
   :health     #'health/health
   :keep-alive #'keep-alive/keep-alive
   :metering   #'metering/metering})

(defn wait! []
  (if-let [^String wait-time (config/conf-prop :wait-ms-on-stop)]
    (try
      (log/info "<- Waiting " wait-time " milliseconds.")
      (Thread/sleep (Integer. wait-time))
      (catch Exception e
        (log/error e)))))

(defn stop [& custom-states]
  (beckon/reinit-all!)
  (log/info "<- System will be stopped. Setting lock.")
  (health/lock-application)
  (wait!)
  (log/info "<- Stopping system.")
  (apply mnt/stop (concat (vals the-states) custom-states)))

(defn start-with-states [& custom-states]
  (log/info "-> Starting the MOUNT-MS base system")
  (apply mnt/start (concat (vals the-states) custom-states))
  (doseq [sig ["INT" "TERM"]]
    (reset! (beckon/signal-atom sig) #{(partial apply stop custom-states)})))

(defn start []
  (start-with-states))
