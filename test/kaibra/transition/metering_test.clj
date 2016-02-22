(ns kaibra.transition.metering-test
  (:require [clojure.test :refer :all]
            [kaibra.transition.metering :as tmet]
            [kaibra.stateful.metering :refer [metering]]
            [kaibra.util.test-utils :as u]
            [metrics.timers :as timers])
  (:import (com.codahale.metrics MetricRegistry)))

(deftest ^:unit metrics-registry-should-contain-correct-names
  (u/with-started-system
    (tmet/timer! "some.name.timer.bar")
    (tmet/gauge! #() "some.name.gauge.bar")
    (tmet/counter! "some.name.counter.bar")
    (tmet/histogram! "some.name.histogram.bar")
    (timers/timer ["direct.usage.timer"])
    (let [names (.getNames ^MetricRegistry (:registry metering))]
      (is (true? (contains? names "some.name.timer.bar")))
      (is (true? (contains? names "some.name.gauge.bar")))
      (is (true? (contains? names "some.name.counter.bar")))
      (is (true? (contains? names "some.name.histogram.bar")))
      (is (true? (contains? names "direct.usage.timer"))))))
