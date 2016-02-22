(ns kaibra.system
  (:require
    [beckon :as beckon]
    [clojure.tools.logging :as log]
    [mount.core :as mnt]
    [kaibra.transition.configuring :as tconf]
    [kaibra.transition.health :as thealth]

    [kaibra.stateful.metering :refer [metering]]
    [kaibra.stateful.keep-alive :refer [keep-alive]]
    [kaibra.stateful.health :refer [health]]
    [kaibra.stateful.app-status :refer [app-status]]
    [kaibra.stateful.configuring :refer [config]]))

(defn wait! []
  (if-let [^String wait-time (tconf/conf-prop :wait-ms-on-stop)]
    (try
      (log/info "<- Waiting " wait-time " milliseconds.")
      (Thread/sleep (Integer. wait-time))
      (catch Exception e
        (log/error e)))))

(defn stop []
  (beckon/reinit-all!)
  (log/info "<- System will be stopped. Setting lock.")
  (thealth/lock-application)
  (wait!)
  (log/info "<- Stopping system.")
  (mnt/stop))

(defn start []
  (mnt/start)
  (doseq [sig ["INT" "TERM"]]
    (reset! (beckon/signal-atom sig) #{stop})))
