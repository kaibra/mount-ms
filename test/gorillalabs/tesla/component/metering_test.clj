(ns gorillalabs.tesla.stateful.metering-test
  (:require [clojure.test :refer :all]
            [gorillalabs.tesla.component.metering :as metering]
            [gorillalabs.tesla.util.test-utils :as u]
            [gorillalabs.tesla :as system]
            [gorillalabs.tesla.component.configuration :as configuring]))




#_(deftest ^:unit metrics-registry-should-contain-correct-names
  (u/with-started [started (dissoc (system/base-system {}) :server)]
                  (let [metering (:metering started)]
                    (metering/timer! metering "some.name.timer.bar")
                    (metering/gauge! metering #() "some.name.gauge.bar")
                    (metering/counter! metering "some.name.counter.bar")
                    (let [names (.getNames (:registry metering))]
                      (is (true? (contains? names "some.name.timer.bar")))
                      (is (true? (contains? names "some.name.gauge.bar")))
                      (is (true? (contains? names "some.name.counter.bar")))))))
