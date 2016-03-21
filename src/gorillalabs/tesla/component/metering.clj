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

(defmulti start-reporter! (fn [reporter _ _] reporter))

(defmethod start-reporter! :graphite [_ registry config]
  (let [prefix (fn prefix [config]
                 (str (config/config config [:metering :graphite :prefix]) "." (config/external-hostname config)))
        reporter (graphite/reporter registry
                                    {:host          (config/config config [:metering :graphite :host])
                                     :port          (int (config/config config [:metering :graphite :port] 2003))
                                     :prefix        (prefix config)
                                     :rate-unit     TimeUnit/SECONDS
                                     :duration-unit TimeUnit/MILLISECONDS
                                     :filter        MetricFilter/ALL})]
    (log/info "-> starting graphite reporter.")
    (graphite/start reporter (int (config/config config [:metering :graphite :interval-seconds] 10)))
    reporter))

(defmethod start-reporter! :console [_ registry config]
  (let [reporter (console/reporter registry {})]
    (log/info "-> starting console reporter.")
    (console/start reporter (int (config/config config [:metering :console :interval-seconds] 10)))
    reporter))

(defn- start-reporters! [registry config]
  (let [reporter-or-reporters (config/config config [:metering :reporter])
        reporters (if (or (nil? reporter-or-reporters)      ;; might be nil
                          (coll? reporter-or-reporters))
                    reporter-or-reporters
                    (vector reporter-or-reporters))]
    (doall (map
             #(start-reporter! %1 registry config)
             reporters
             ))))



(defn- start []
  (log/info "-> starting metering.")
  (let [registry (metrics/new-registry)
        config config/configuration]
    {:registry  registry
     :reporters (start-reporters! registry config)}))

(defn- stop [metering]
  (log/info "<- stopping metering")
  (when-let [reporter (:reporters metering)]
    (doall (map (fn stop-reporter [r]
                  (log/info "stopping " r)
                  (.stop r)) reporter)))
  metering)

;; Initialises a metrics-registry and a graphite reporter.
(mnt/defstate ^{:on-reload :noop}
              metering
              :start (start)
              :stop (stop metering))

(defn gauge! [name gauge-callback-fn]
  (gauges/gauge-fn (:registry metering) [name] gauge-callback-fn))

(defn timer! [name]
  (timers/timer (:registry metering) [name]))

(defn counter! [name]
  (counters/counter (:registry metering) [name]))