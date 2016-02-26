(ns kaibra.system
  (:require
    [beckon :as beckon]
    [clojure.tools.logging :as log]
    [mount.core :as mnt]
    [kaibra.stateful.health :refer [health] :as hlth]
    [kaibra.stateful.configuring :refer [config] :as conf]
    [kaibra.stateful.metering :refer [metering]]
    [kaibra.stateful.keep-alive :refer [keep-alive]]
    [kaibra.stateful.app-status :refer [app-status]]))

(defn wait! []
  (if-let [^String wait-time (conf/conf-prop :wait-ms-on-stop)]
    (try
      (log/info "<- Waiting " wait-time " milliseconds.")
      (Thread/sleep (Integer. wait-time))
      (catch Exception e
        (log/error e)))))

(defn stop []
  (beckon/reinit-all!)
  (log/info "<- System will be stopped. Setting lock.")
  (hlth/lock-application)
  (wait!)
  (log/info "<- Stopping system.")
  (mnt/stop))

(defn start []
  (log/info "-> Starting the MOUNT-MS base system")
  (mnt/start)
  (doseq [sig ["INT" "TERM"]]
    (reset! (beckon/signal-atom sig) #{stop})))
