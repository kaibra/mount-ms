;;; Telemetry component
;;;
;;; Sample configuration:
;;;
;;; {:telemetry {:riemann  {:host "127.0.0.2" :port 5555}
;;;              :host     "app.eu.backend42" ;; leave out for automatic hostname detection
;;;              :prefix   "app.backend."     ;; automatically prefix all service names
;;;              :interval 60                 ;; seconds
;;;              }}

(ns gorillalabs.tesla.component.telemetry
  (:require [clojure.core.async :refer [<! >!! sliding-buffer chan timeout poll! close! go-loop alt!]]
            [clojure.tools.logging :as log]
            [gorillalabs.tesla.component.configuration :as config]
            [mount.core :as mnt]
            [riemann.client :as riemann])
  (:import (java.net InetAddress)))

(defn- now []
  (/ (System/currentTimeMillis) 1000))

(defn- localhost []
  (let [addr (InetAddress/getLocalHost)]
    (.getHostName addr)))

(defn- drain-queue [queue]
  (loop [items []]
    (if-let [item (poll! queue)]
      (recur (conj items item))
      items)))

(defn- flush-queue [client queue]
  (let [events (drain-queue queue)]
    (when (and client (seq events))
      (log/infof "Sending %d event(s) to telemetry backend..." (count events))
      (riemann/send-events client events))))

(defn- prepare-event [telemetry event]
  (let [prefix (:prefix (:config telemetry) "")]
    (-> event
      (assoc :time (now))
      (assoc :host (:host telemetry))
      (assoc :service (str prefix (:service event))))))

(defn enqueue [telemetry event]
  (>!! (:queue telemetry) (prepare-event telemetry event)))

(defn state [telemetry service state]
  (enqueue telemetry {:service service :state state}))

(defn custom [telemetry message]
  (enqueue telemetry message))

(defmacro timed
  "Wraps expr in a timer and reports the elapsed time as a metric named with identifier."
  [identifier expr]
  (let [start (gensym)
        result (gensym)
        elapsed (gensym)]
    (list 'if (list :host 'gorillalabs.tesla.component.telemetry/telemetry)
          (list 'let [start   (list 'System/currentTimeMillis)
                      result  expr
                      elapsed (list '- (list 'System/currentTimeMillis) start)]
                (list 'log/infof "[%s] took %dms." identifier elapsed)
                (list 'gorillalabs.tesla.component.telemetry/enqueue 'gorillalabs.tesla.component.telemetry/telemetry {:service identifier :metric elapsed})
                result)
          expr)))

(defn- worker [killswitch client queue interval]
  (go-loop []
    (let [timeout (timeout (* interval 1000))]
      (alt!
        killswitch :end
        timeout (do (flush-queue client queue)
                    (recur))))))

(defn- start []
  (log/info "-> starting telemetry")
  (let [config     (config/config config/configuration [:telemetry])
        queue      (chan (sliding-buffer 100))
        killswitch (chan)
        client     (when (:riemann config) (riemann/tcp-client (:riemann config)))]
    (worker killswitch client queue (:interval config 30))
    {:host       (:host config (localhost))
     :r-client   client
     :queue      queue
     :config     config
     :killswitch killswitch}))

(defn- stop [telemetry]
  (log/info "<- stopping telemetry")
  (close! (:killswitch telemetry)))

(mnt/defstate telemetry
  :start (start)
  :stop (stop telemetry))
