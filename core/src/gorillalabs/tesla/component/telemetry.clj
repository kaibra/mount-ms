(ns gorillalabs.tesla.component.telemetry
  (:require [clojure.core.async :refer [<! >!! sliding-buffer chan timeout poll! close! go-loop]]
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
    (when (seq events)
      (log/infof "Sending %d event(s) to telemetry backend..." (count events))
      (riemann/send-events client events))))

(defn enqueue [telemetry event]
  (>!! (:queue telemetry) (assoc event :time (now) :host (:host telemetry))))

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
    (list 'if 'gorillalabs.tesla.component.telemetry/telemetry
          (list 'let [start   (list 'System/currentTimeMillis)
                      result  expr
                      elapsed (list '- (list 'System/currentTimeMillis) start)]
                (list 'log/infof "[%s] took %dms." identifier elapsed)
                (list 'gorillalabs.tesla.component.telemetry/enqueue 'gorillalabs.tesla.component.telemetry/telemetry {:service identifier :metric elapsed})
                result)
          expr)))

(defn- worker [client queue interval]
  (go-loop []
    (<! (timeout (* interval 1000)))
    (when (flush-queue client queue)
      (recur))))

(defn- start []
  (when-let [config (config/config config/configuration [:telemetry])]
    (log/info "-> starting telemetry")
    (let [queue  (chan (sliding-buffer 100))
          client (riemann/tcp-client (:riemann config))]
      (worker client queue (:interval config 30))
      {:host     (:host config (localhost))
       :r-client client
       :queue    queue})))

(defn- stop [telemetry]
  (log/info "<- stopping telemetry")
  (close! (:queue telemetry)))

(mnt/defstate telemetry
  :start (start)
  :stop (stop telemetry))
