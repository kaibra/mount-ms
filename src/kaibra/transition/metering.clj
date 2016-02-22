(ns kaibra.transition.metering
  (:require
    [metrics.timers :as timers]
    [metrics.counters :as counters]
    [metrics.gauges :as gauges]
    [metrics.histograms :as histograms]
    [kaibra.stateful.metering :refer [metering]]))

(defn gauge! [gauge-callback-fn name#]
  (gauges/gauge-fn (:registry metering) [name#] gauge-callback-fn))

(defn timer! [name#]
  (timers/timer (:registry metering) [name#]))

(defn counter! [name#]
  (counters/counter (:registry metering) [name#]))

(defn histogram! [name#]
  (histograms/histogram (:registry metering) [name#]))
