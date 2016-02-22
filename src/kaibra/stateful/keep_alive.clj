(ns kaibra.stateful.keep-alive
  "This component is responsible for keeping the system alive by creating a non-deamonized noop thread."
  (:require [clojure.tools.logging :as log]
            [mount.core :refer [defstate]]))

(defn do-nothing []
  (while true (Thread/sleep 60000)))

(defn stop-keepalive [self]
  (log/info "<- stopping keepalive thread.")
  (.stop self))

(defn start-keep-alive []
  (log/info "-> starting keepalive thread.")
  (let [thread (Thread. do-nothing)]
    (.start thread)
    thread))

(defstate keep-alive
          :start (start-keep-alive)
          :stop (stop-keepalive keep-alive))
