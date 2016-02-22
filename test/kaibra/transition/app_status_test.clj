(ns kaibra.transition.app-status-test
  (:require [clojure.test :refer :all]
            [kaibra.transition.app-status :as t-app-status]
            [environ.core :as env]
            [clojure.data.json :as json]
            [kaibra.util.test-utils :as u]
            [ring.mock.request :as mock]))

(def status-response-body #'t-app-status/status-response-body)
(def status-response #'t-app-status/status-response)
(def sanitize #'t-app-status/sanitize)
(def app-status-handler #'t-app-status/app-status-handler)

(deftest ^:unit should-have-system-status-for-runtime-config
  (u/with-runtime-config
    {:host-name "bar" :server-port "0123"}
    (u/with-app-status
      (let [system-status (:system (status-response-body))]
        (is (= (:hostname system-status) "bar"))
        (is (= (:port system-status) "0123"))
        (is (not (nil? (:systemTime system-status))))))))

(deftest ^:unit host-name-and-port-on-app-status
  (testing "should add host and port from env to app-status in property-file case"
    (with-redefs [env/env {:host-name "foo" :server-port "1234"}]
      (u/with-runtime-config
        {:property-file-preferred true}
        (u/with-app-status
          (let [system-status (:system (status-response-body))]
            (is (= (:hostname system-status) "foo"))
            (is (= (:port system-status) "1234"))
            (is (not (nil? (:systemTime system-status))))))))
    (testing "should add host and port from env to app-status in edn-file case"
      (u/with-app-status
        (let [system-status (:system (status-response-body))]
          (is (= (:hostname system-status) "localhost"))
          (is (= (:port system-status) "9991"))
          (is (not (nil? (:systemTime system-status)))))))))

(deftest ^:unit should-sanitize-passwords
  (let [runtime-conf {:somerandomstuff                        "not-so-secret"
                      :somerandomstuff-passwd-somerandomstuff "secret"
                      :somerandomstuff-pwd-somerandomstuff    "secret"}]
    (u/with-runtime-config
      runtime-conf
      (u/with-config
        (is (= {:somerandomstuff                        "not-so-secret"
                :somerandomstuff-passwd-somerandomstuff "******"
                :somerandomstuff-pwd-somerandomstuff    "******"}
               (select-keys (sanitize ["passwd" "pwd"])
                            #{:somerandomstuff
                              :somerandomstuff-passwd-somerandomstuff
                              :somerandomstuff-pwd-somerandomstuff})))))))

(deftest ^:unit should-show-applicationstatus
  (u/with-app-status
    (t-app-status/register-status-fun (constantly {:mock {:status  :ok
                                                          :message "nevermind"}}))
    (let [application-body (get (json/read-str (:body (status-response))) "application")]
      (testing "it shows OK as application status"
        (is (= (get application-body "status")
               "OK")))

      (testing "it shows the substatus"
        (is (= (get application-body "statusDetails")
               {"mock" {"message" "nevermind" "status" "OK"}}))))))

(deftest ^:unit should-show-error-as-application-status
  (u/with-app-status
    (t-app-status/register-status-fun (constantly {:mock {:status  :error
                                                          :message "nevermind"}}))
    (let [applicationStatus (get (get (json/read-str (:body (status-response))) "application") "status")]
      (is (= applicationStatus "ERROR")))))

(deftest ^:integration should-serve-status-under-configured-url
  (testing "use the default url"
    (u/with-app-status
      (is (= 200 (:status ((app-status-handler) (mock/request :get "/status")))))))

  (testing "use the configuration url"
    (u/with-runtime-config
      {:status-url "/my-status"}
      (u/with-app-status
        (is (= 200 (:status ((app-status-handler) (mock/request :get "/my-status"))))))))

  (testing "default should be overridden"
    (u/with-runtime-config
      {:status-url "/my-status"}
      (u/with-app-status
        (is (= nil ((app-status-handler) (mock/request :get "/status"))))))))

(deftest should-add-version-properties-to-status
  (testing "it should add the version properties"
    (u/with-app-status
      (let [status-map (json/read-json (:body ((app-status-handler) (mock/request :get "/status"))))]
        (is (= (get-in status-map [:application :version]) "test.version"))
        (is (= (get-in status-map [:application :git]) "test.githash"))))))
