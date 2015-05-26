(ns tesla.component.metering-test
  (:import (java.net UnknownHostException))
  (:require [clojure.test :refer :all]
            [tesla.component.metering :as metering]
            [tesla.util.test-utils :as u]
            [tesla.system :as system]
            [tesla.component.configuring :as configuring]))


(deftest ^:unit should-return-prefix-for-testhost
  #(is (= (metering/prefix {:config {:metering {:graphite-prefix "a_random_prefix"}}})
          "a_random_prefix")))

(deftest ^:unit the-metrics-lib-accepts-a-vector-for-building-the-name
  (is (= (metrics.core/metric-name ["some.name.foo.bar"])
         "some.name.foo.bar"))
  (is (= (metrics.core/metric-name ["some" "name" "foo" "bar"])
         "some.name.foo.bar")))

(deftest ^:unit metrics-registry-should-contain-correct-names
  (u/with-started [started (dissoc (system/empty-system {}) :server)]
                  (let [metering (:metering started)]
                    (metering/timer! metering "some.name.timer.bar")
                    (metering/gauge! metering #() "some.name.gauge.bar")
                    (metering/counter! metering "some.name.counter.bar")
                    (let [names (.getNames (:registry metering))]
                      (is (true? (contains? names "some.name.timer.bar")))
                      (is (true? (contains? names "some.name.gauge.bar")))
                      (is (true? (contains? names "some.name.counter.bar")))))))
