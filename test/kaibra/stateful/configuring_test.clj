(ns kaibra.stateful.configuring-test
  (:require [clojure.test :refer :all]
            [kaibra.stateful.configuring :as conf]
            [clojure.java.io :as io]
            [test.kaibra.utils :as u]
            [environ.core :as env]))

(def load-config-from-property-files #'conf/load-config-from-property-files)
(def load-config-from-edn-files #'conf/load-config-from-edn-files)

(deftest referencing-env-properties
  (testing "should return env-property if referenced in edn-config"
    (u/with-states [:config]
                   :env {:prop-without-fallback "prop-value"}
                   (is (= "prop-value" (get-in conf/config [:config :prop-without-fallback])))))
  (testing "should return empty if env prop does not exist and fallback not provided"
    (u/with-states [:config]
                   (is (= "" (get-in conf/config [:config :prop-without-fallback]))))))

(deftest ^:unit should-read-property-from-default-config
  (testing "should be possible to prefer reading configs from property files"
    (u/with-states [:config]
                   :runtime-config {:property-file-preferred true}
                   (is (= (get-in conf/config [:config :foo-prop]) "baz"))
                   (is (= (get-in conf/config [:config :foo :edn]) nil)))))

(deftest ^:unit should-read-property-from-default-edn-file
  (u/with-states
    [:config]
    (is (= (get-in conf/config [:config :foo-prop]) nil))
    (is (= (get-in conf/config [:config :foo :edn]) "baz"))))

(deftest ^:unit should-read-property-from-custom-edn-file
  (u/with-states
    [:config]
    :env {:config-file "test.edn"}
    (is (= (get-in conf/config [:config :health-url]) "/test/health"))))

(deftest ^:unit should-read-property-from-runtime-config
  (u/with-states [:config]
                 :runtime-config {:foo-rt "bat" :fooz {:nested 123}}
                 (is (= (get-in conf/config [:config :foo-prop]) nil))
                 (is (= (get-in conf/config [:config :foo-rt]) "bat"))
                 (is (= (get-in conf/config [:config :foo :edn]) "baz"))
                 (is (= (get-in conf/config [:config :fooz :nested]) 123))))

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

(deftest ^:unit determine-hostname-from-config-and-env-with-defined-precedence
  (testing "it prefers a explicitly configured :host-name"
    (u/with-states [:config]
                   :env {:host "host" :host-name "host-name" :hostname "hostname"}
                   :runtime-config {:host-name "configured"}
                   (is (= "configured" (conf/external-hostname)))))

  (testing "it falls back to env-vars and prefers $HOST"
    (with-redefs [env/env {:host "host" :host-name "host-name" :hostname "hostname"}]
      (u/with-states [:config]
                     (is (= "host" (conf/external-hostname))))))

  (testing "it falls back to env-vars and prefers $HOST_NAME"
    (with-redefs [env/env {:host-name "host-name" :hostname "hostname"}]
      (u/with-states [:config]
                     (is (= "host-name" (conf/external-hostname))))))

  (testing "it falls back to env-vars and looks finally for $HOSTNAME"
    (with-redefs [env/env {:hostname "hostname"}]
      (u/with-states [:config]
                     (is (= "hostname" (conf/external-hostname))))))

  (testing "it eventually falls back to localhost"
    (u/with-states [:config]
                   (is (= "localhost" (conf/external-hostname))))))

(deftest ^:unit determine-hostport-from-config-and-env-with-defined-precedence
  (with-redefs [conf/load-config-from-edn-files (constantly {})]

    (testing "it prefers a explicitly configured :hostname"
      (u/with-states [:config]
                     :runtime-config {:server-port "configured"}
                     :env {:port0 "0" :host-port "1" :server-port "2"}
                     (is (= "configured" (conf/external-port)))))

    (testing "it falls back to env-vars and prefers $PORT0"
      (u/with-states [:config]
                     :env {:port0 "0" :host-port "1" :server-port "2"}
                     (is (= "0" (conf/external-port)))))

    (testing "it falls back to env-vars and prefers $HOST_PORT"
      (u/with-states [:config]
                     :env {:host-port "1" :server-port "2"}
                     (is (= "1" (conf/external-port)))))

    (testing "it falls back to env-vars and finally takes $SERVER_PORT"
      (u/with-states [:config]
                     :env {:server-port "2"}
                     (is (= "2" (conf/external-port)))))))
