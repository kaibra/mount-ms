(ns kaibra.stateful.configuring-test
  (:require [clojure.test :refer :all]
            [kaibra.stateful.configuring :as configuring]
            [clojure.java.io :as io]
            [kaibra.util.test-utils :as u]
            [environ.core :as env]))

(def load-config-from-property-files #'configuring/load-config-from-property-files)
(def load-config-from-edn-files #'configuring/load-config-from-edn-files)

(deftest referencing-env-properties
  (testing "should return env-property if referenced in edn-config"
    (with-redefs [env/env {:prop-without-fallback "prop-value"}]
      (u/with-config
        (is (= "prop-value" (get-in configuring/config [:config :prop-without-fallback]))))))
  (testing "should return empty if env prop does not exist and fallback not provided"
    (with-redefs [env/env {}]
      (u/with-config
        (is (= "" (get-in configuring/config [:config :prop-without-fallback])))))))

(deftest ^:unit should-read-property-from-default-config
  (testing "should be possible to prefer reading configs from property files"
    (u/with-runtime-config
      {:property-file-preferred true}
      (u/with-config
        (is (= (get-in configuring/config [:config :foo-prop]) "baz"))
        (is (= (get-in configuring/config [:config :foo :edn]) nil))))))

(deftest ^:unit should-read-property-from-default-edn-file
  (u/with-config
    (is (= (get-in configuring/config [:config :foo-prop]) nil))
    (is (= (get-in configuring/config [:config :foo :edn]) "baz"))))

(deftest ^:unit should-read-property-from-custom-edn-file
  (with-redefs [env/env {:config-file "test.edn"}]
    (u/with-config
      (is (= (get-in configuring/config [:config :health-url]) "/test/health")))))

(deftest ^:unit should-read-property-from-runtime-config
  (u/with-runtime-config
    {:foo-rt "bat" :fooz {:nested 123}}
    (u/with-config
      (is (= (get-in configuring/config [:config :foo-prop]) nil))
      (is (= (get-in configuring/config [:config :foo-rt]) "bat"))
      (is (= (get-in configuring/config [:config :foo :edn]) "baz"))
      (is (= (get-in configuring/config [:config :fooz :nested]) 123)))))

(deftest ^:unit should-read-default-properties
  (testing "should read default properties from property-files"
    (let [loaded-properties (load-config-from-property-files)]
      (is (not (nil? (:server-port loaded-properties))))
      (is (not (nil? (:metering-reporter loaded-properties))))))

  (testing "should read default properties from edn-property-files"
    (let [loaded-properties (load-config-from-edn-files)]
      (is (not (nil? (:server-port loaded-properties))))
      (is (not (nil? (:metering-reporter loaded-properties)))))))

(deftest ^:integration should-read-properties-from-file
  (spit "application.properties" "foooo=barrrr")
  (is (= (:foooo (load-config-from-property-files))
         "barrrr"))
  (io/delete-file "application.properties"))

(deftest ^:integration should-prefer-configured-conf-file
  (spit "application.properties" "foooo=value")
  (spit "other.properties" "foooo=other-value")
  (with-redefs-fn {#'env/env {:config-file "other.properties"}}
    #(is (= (:foooo (load-config-from-property-files))
            "other-value")))
  (io/delete-file "other.properties")
  (io/delete-file "application.properties"))
