(ns kaibra.stateful.metering
  (:require
    [mount.core :refer [defstate]]
    [metrics.core :as metrics]
    [metrics.timers :as timers]
    [metrics.counters :as counters]
    [metrics.gauges :as gauges]
    [metrics.histograms :as histograms]
    [metrics.reporters.graphite :as graphite]
    [metrics.reporters.console :as console]
    [clojure.tools.logging :as log]
    [kaibra.stateful.configuring :as conf])
  (:import
    (com.codahale.metrics MetricFilter)
    (java.util.concurrent TimeUnit)))

(defn- short-hostname [hostname]
  (re-find #"[^.]*" hostname))

(defn- graphite-host-prefix []
  (let [external-hostname (conf/external-hostname)
        hostname (if (conf/conf-prop :graphite-shorten-hostname?)
                   (short-hostname external-hostname)
                   external-hostname)]
    (str (conf/conf-prop :graphite-prefix) "." hostname)))

(defn graphite-conf []
  {:host          (conf/conf-prop :graphite-host)
   :port          (Integer. (conf/conf-prop :graphite-port))
   :prefix        (graphite-host-prefix)
   :rate-unit     TimeUnit/SECONDS
   :duration-unit TimeUnit/MILLISECONDS
   :filter        MetricFilter/ALL})

(defn- start-graphite! [registry]
  (let [graphite-conf (graphite-conf)
        reporter (graphite/reporter registry graphite-conf)]
    (log/info "-> starting graphite reporter:" graphite-conf)
    (graphite/start reporter (Integer/parseInt (conf/conf-prop :graphite-interval-seconds)))
    reporter))

(defn- start-console! [registry]
  (let [reporter (console/reporter registry {})]
    (log/info "-> starting console reporter.")
    (console/start reporter (Integer/parseInt (conf/conf-prop :console-interval-seconds)))
    reporter))

(defn- start-reporter! [registry]
  (case (conf/conf-prop :metering-reporter)
    "graphite" (start-graphite! registry)
    "console" (start-console! registry)
    nil                                                     ;; default: do nothing!
    ))

(defn start-metering []
  (log/info "-> starting metering.")
  (let [registry metrics/default-registry]
    {:registry registry
     :reporter (start-reporter! registry)}))

(defn stop-metering [self]
  (log/info "<- stopping metering")
  (when-let [reporter (:reporter self)]
    (.stop reporter)))

(defstate metering
          :start (start-metering)
          :stop (stop-metering metering))

(defn gauge! [gauge-callback-fn name#]
  (gauges/gauge-fn (:registry metering) [name#] gauge-callback-fn))

(defn timer! [name#]
  (timers/timer (:registry metering) [name#]))

(defn counter! [name#]
  (counters/counter (:registry metering) [name#]))

(defn histogram! [name#]
  (histograms/histogram (:registry metering) [name#]))
