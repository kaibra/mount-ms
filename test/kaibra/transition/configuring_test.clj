(ns kaibra.transition.configuring-test
  (:require [clojure.test :refer :all]
            [kaibra.transition.configuring :as tconf]
            [kaibra.stateful.configuring :as conf]
            [kaibra.util.test-utils :as u]
            [environ.core :as env]))

(deftest ^:unit determine-hostname-from-config-and-env-with-defined-precedence
  (testing "it prefers a explicitly configured :host-name"
    (with-redefs [env/env {:host "host" :host-name "host-name" :hostname "hostname"}]
      (u/with-runtime-config
        {:host-name "configured"}
        (u/with-config
          (is (= "configured" (tconf/external-hostname)))))))

  (testing "it falls back to env-vars and prefers $HOST"
    (with-redefs [env/env {:host "host" :host-name "host-name" :hostname "hostname"}]
      (u/with-config
        (is (= "host" (tconf/external-hostname))))))

  (testing "it falls back to env-vars and prefers $HOST_NAME"
    (with-redefs [env/env {:host-name "host-name" :hostname "hostname"}]
      (u/with-config
        (is (= "host-name" (tconf/external-hostname))))))

  (testing "it falls back to env-vars and looks finally for $HOSTNAME"
    (with-redefs [env/env {:hostname "hostname"}]
      (u/with-config
        (is (= "hostname" (tconf/external-hostname))))))

  (testing "it eventually falls back to localhost"
    (u/with-config
      (is (= "localhost" (tconf/external-hostname))))))

(deftest ^:unit determine-hostport-from-config-and-env-with-defined-precedence
  (with-redefs [conf/load-config-from-edn-files (constantly {})]

    (testing "it prefers a explicitly configured :hostname"
      (with-redefs [env/env {:port0 "0" :host-port "1" :server-port "2"}]
        (u/with-runtime-config
          {:server-port "configured"}
          (u/with-config
            (is (= "configured" (tconf/external-port)))))))

    (testing "it falls back to env-vars and prefers $PORT0"
      (with-redefs [env/env {:port0 "0" :host-port "1" :server-port "2"}]
        (u/with-config
          (is (= "0" (tconf/external-port))))))

    (testing "it falls back to env-vars and prefers $HOST_PORT"
      (with-redefs [env/env {:host-port "1" :server-port "2"}]
        (u/with-config
          (is (= "1" (tconf/external-port))))))

    (testing "it falls back to env-vars and finally takes $SERVER_PORT"
      (with-redefs [env/env {:server-port "2"}]
        (u/with-config
          (is (= "2" (tconf/external-port))))))))
