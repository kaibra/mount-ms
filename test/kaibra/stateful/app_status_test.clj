(ns kaibra.stateful.app-status-test
  (:require [clojure.test :refer :all]
            [kaibra.stateful.app-status :as app-status]
            [test.kaibra.utils :as u]
            [de.otto.status :as s]
            [ring.mock.request :as mock]
            [clojure.data.json :as json]))

(def status-response-body #'app-status/status-response-body)
(def status-response #'app-status/status-response)
(def sanitize #'app-status/sanitize)
(def app-status-handler #'app-status/app-status-handler)
(def aggregation-strategy #'app-status/aggregation-strategy)

(deftest determine-status-strategy
  (testing "it should use strict stategy if none is configured"
    (u/with-states
      [:config]
      :runtime-config {:status-aggregation nil}
      (is (= s/strict-strategy (aggregation-strategy)))))

  (testing "it should use forgiving stategy if forgiving is configured"
    (u/with-states
      [:config]
      :runtime-config {:status-aggregation "forgiving"}
      (is (= s/forgiving-strategy (aggregation-strategy)))))

  (testing "it should use strict stategy if something else is configured"
    (u/with-states
      [:config]
      :runtime-config {:status-aggregation "unknown"}
      (is (= s/strict-strategy (aggregation-strategy))))))

(deftest ^:unit should-have-system-status-for-runtime-config
  (u/with-states
    [:app-status :config]
    :runtime-config {:host-name "bar" :server-port "0123"}
    (let [system-status (:system (status-response-body))]
      (is (= (:hostname system-status) "bar"))
      (is (= (:port system-status) "0123"))
      (is (not (nil? (:systemTime system-status)))))))

(deftest ^:unit host-name-and-port-on-app-status
  (testing "should add host and port from env to app-status in property-file case"
    (u/with-states
      [:app-status :config]
      :runtime-config {:property-file-preferred true}
      :env {:host-name "foo" :server-port "1234"}
      (let [system-status (:system (status-response-body))]
        (is (= (:hostname system-status) "foo"))
        (is (= (:port system-status) "1234"))
        (is (not (nil? (:systemTime system-status)))))))
  (testing "should add host and port from env to app-status in edn-file case"
    (u/with-states
      [:app-status :config]
      (let [system-status (:system (status-response-body))]
        (is (= (:hostname system-status) "localhost"))
        (is (= (:port system-status) "9991"))
        (is (not (nil? (:systemTime system-status))))))))

(deftest ^:unit should-sanitize-passwords
  (u/with-states
    [:config]
    :runtime-config {:somerandomstuff                        "not-so-secret"
                     :somerandomstuff-passwd-somerandomstuff "secret"
                     :somerandomstuff-pwd-somerandomstuff    "secret"}
    (is (= {:somerandomstuff                        "not-so-secret"
            :somerandomstuff-passwd-somerandomstuff "******"
            :somerandomstuff-pwd-somerandomstuff    "******"}
           (select-keys (sanitize ["passwd" "pwd"])
                        #{:somerandomstuff
                          :somerandomstuff-passwd-somerandomstuff
                          :somerandomstuff-pwd-somerandomstuff})))))

(deftest ^:unit should-show-applicationstatus
  (u/with-states
    [:app-status :config]
    (app-status/register-status-fun (constantly {:mock {:status  :ok
                                                        :message "nevermind"}}))
    (let [application-body (get (json/read-str (:body (status-response))) "application")]
      (testing "it shows OK as application status"
        (is (= (get application-body "status")
               "OK")))

      (testing "it shows the substatus"
        (is (= (get application-body "statusDetails")
               {"mock" {"message" "nevermind" "status" "OK"}}))))))

(deftest ^:unit should-show-error-as-application-status
  (u/with-states
    [:app-status :config]
    (app-status/register-status-fun (constantly {:mock {:status  :error
                                                        :message "nevermind"}}))
    (let [applicationStatus (get (get (json/read-str (:body (status-response))) "application") "status")]
      (is (= applicationStatus "ERROR")))))

(deftest ^:integration should-serve-status-under-configured-url
  (testing "use the default url"
    (u/with-states
      [:app-status :config]
      (is (= 200 (:status ((app-status-handler) (mock/request :get "/status")))))))

  (testing "use the configuration url"
    (u/with-states
      [:app-status :config]
      :runtime-config {:status-url "/my-status"}
      (is (= 200 (:status ((app-status-handler) (mock/request :get "/my-status")))))))

  (testing "default should be overridden"
    (u/with-states
      [:app-status :config]
      :runtime-config {:status-url "/my-status"}
      (is (= nil ((app-status-handler) (mock/request :get "/status")))))))

(deftest should-add-version-properties-to-status
  (testing "it should add the version properties"
    (u/with-states
      [:app-status :config]
      (let [status-map (json/read-json (:body ((app-status-handler) (mock/request :get "/status"))))]
        (is (= (get-in status-map [:application :version]) "test.version"))
        (is (= (get-in status-map [:application :git]) "test.githash"))))))

