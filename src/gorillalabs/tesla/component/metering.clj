(ns gorillalabs.tesla.component.metering
  (:require
    [mount.core :as mnt]
    [metrics.core :as metrics]
    [metrics.timers :as timers]
    [metrics.counters :as counters]
    [metrics.gauges :as gauges]
    [metrics.reporters.graphite :as graphite]
    [metrics.reporters.console :as console]
    [clojure.tools.logging :as log]
    [gorillalabs.tesla.component.configuration :as config])
  (:import
    (com.codahale.metrics MetricFilter)
    (java.util.concurrent TimeUnit)))

(defn prefix [config]
  (str (:graphite-prefix config) "." (config/external-hostname config)))

(defn start-graphite! [registry config]
  (let [reporter (graphite/reporter registry
                                    {:host          (:graphite-host config)
                                     :port          (Integer. (:graphite-port config))
                                     :prefix        (prefix config)
                                     :rate-unit     TimeUnit/SECONDS
                                     :duration-unit TimeUnit/MILLISECONDS
                                     :filter        MetricFilter/ALL})]
    (log/info "-> starting graphite reporter.")
    (graphite/start reporter (Integer/parseInt (:graphite-interval-seconds config)))
    reporter))

(defn start-console! [registry config]
  (let [reporter (console/reporter registry {})]
    (log/info "-> starting console reporter.")
    (console/start reporter (Integer/parseInt (:console-interval-seconds config "10")))
    reporter))

(defn- start-reporter! [registry config]
  (case (:metering-reporter (:config config))
    "graphite" (start-graphite! registry config)
    "console" (start-console! registry config)
    nil                                                     ;; default: do nothing!
    ))





(defn- start []
  (log/info "-> starting metering.")
  (let [registry (metrics/new-registry)
        config config/configuration]
    {:registry registry
     :reporter (start-reporter! registry config)}))

(defn- stop [self]
  (log/info "<- stopping metering")
  (when-let [reporter (:reporter self)]
    (.stop reporter))
  self)

;; Initialises a metrics-registry and a graphite reporter.
(mnt/defstate metering
              :start (start)
              :stop (stop metering))

(defn gauge! [name gauge-callback-fn]
  (gauges/gauge-fn (:registry metering) [name] gauge-callback-fn))

(defn timer! [name]
  (timers/timer (:registry metering) [name]))

(defn counter! [name]
  (counters/counter (:registry metering) [name]))