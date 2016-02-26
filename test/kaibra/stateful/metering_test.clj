(ns kaibra.stateful.metering-test
  (:require [clojure.test :refer :all]
            [kaibra.stateful.metering :as metering]
            [kaibra.stateful.configuring :as conf]
            [kaibra.util.test-utils :as u]))

(def graphite-host-prefix #'metering/graphite-host-prefix)
(deftest ^:unit graphite-prefix-test
  (with-redefs [conf/external-hostname (constantly "testhost.example.com")]
    (testing "returns prefix for testhost"
      (u/with-runtime-config
        {:graphite-prefix "a-prefix"}
        (u/with-config
          (is (= "a-prefix.testhost.example.com"
                 (graphite-host-prefix))))))
    (testing "returns prefix for testhost2"
      (u/with-runtime-config
        {:graphite-shorten-hostname? true
         :graphite-prefix            "a-prefix"}
        (u/with-config
          (is (= "a-prefix.testhost"
                 (graphite-host-prefix))))))))

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