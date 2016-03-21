(ns gorillalabs.tesla.component.metering-test
  (:require [gorillalabs.tesla.component.metering :as metering]
            [gorillalabs.tesla.component.configuration :as configuration]
            [clojure.test :refer :all]
            )
  (:import (com.codahale.metrics MetricRegistry ConsoleReporter)
           (com.codahale.metrics.graphite GraphiteReporter)))



(deftest test-start-metering
  (testing "Empty configuration should not start any reporters, but have a registry."
    (with-redefs [configuration/configuration {}]
      (let [{:keys [registry reporters] :as metering} (#'metering/start)]
        (is (instance? MetricRegistry registry))
        (is (empty? reporters))
        (#'metering/stop metering)
        )))

  (testing "Should be able to start single reporter"
    (with-redefs [configuration/configuration {:metering {:reporter :console}}]
      (let [{:keys [registry reporters] :as metering} (#'metering/start)]
        (is (instance? MetricRegistry registry))
        (is (coll? reporters))
        (is (instance? ConsoleReporter (first reporters)))
        (#'metering/stop metering)
        )))

  (testing "Should be able to start multiple reporters"
    (with-redefs [configuration/configuration {:metering {:reporter [:console :graphite]}}]
      (let [{:keys [registry reporters] :as metering} (#'metering/start)]
        (is (instance? MetricRegistry registry))
        (is (coll? reporters))
        (is (instance? ConsoleReporter (first reporters)))
        (is (instance? GraphiteReporter (second reporters)))
        (#'metering/stop metering)
        ))))
