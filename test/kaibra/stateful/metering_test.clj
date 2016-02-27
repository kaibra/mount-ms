(ns kaibra.stateful.metering-test
  (:require [clojure.test :refer :all]
            [kaibra.stateful.metering :as metering]
            [kaibra.stateful.configuring :as conf]
            [kaibra.util.test-utils :as u]
            [kaibra.stateful.metering :as met]
            [metrics.timers :as timers])
  (:import (com.codahale.metrics MetricRegistry)))

(def graphite-host-prefix #'metering/graphite-host-prefix)
(deftest ^:unit graphite-prefix-test
  (with-redefs [conf/external-hostname (constantly "testhost.example.com")]
    (testing "returns prefix for testhost"
      (u/with-states [:config]
                     :runtime-config {:graphite-prefix "a-prefix"}
                     (is (= "a-prefix.testhost.example.com"
                            (graphite-host-prefix)))))
    (testing "returns prefix for testhost2"
      (u/with-states [:config]
                     :runtime-config {:graphite-shorten-hostname? true
                                      :graphite-prefix            "a-prefix"}
          (is (= "a-prefix.testhost"
                 (graphite-host-prefix)))))))

(deftest ^:unit the-metrics-lib-accepts-a-vector-for-building-the-name
  (is (= (metrics.core/metric-name ["some.name.foo.bar"])
         "some.name.foo.bar"))
  (is (= (metrics.core/metric-name ["some" "name" "foo" "bar"])
         "some.name.foo.bar")))

(def short-hostname #'metering/short-hostname)
(deftest short-hostname-test
  (testing "it only returns the important part of a full-qualified hostname"
    (is (= ""
           (short-hostname "")))
    (is (= "some-random-host"
           (short-hostname "some-random-host.foo.bar.baz.com")))))

(deftest ^:unit metrics-registry-should-contain-correct-names
  (u/with-started-system
    (met/timer! "some.name.timer.bar")
    (met/gauge! #() "some.name.gauge.bar")
    (met/counter! "some.name.counter.bar")
    (met/histogram! "some.name.histogram.bar")
    (timers/timer ["direct.usage.timer"])
    (let [names (.getNames ^MetricRegistry (:registry met/metering))]
      (is (true? (contains? names "some.name.timer.bar")))
      (is (true? (contains? names "some.name.gauge.bar")))
      (is (true? (contains? names "some.name.counter.bar")))
      (is (true? (contains? names "some.name.histogram.bar")))
      (is (true? (contains? names "direct.usage.timer"))))))