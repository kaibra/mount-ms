(ns gorillalabs.tesla.stateful.appstate-test
  (:require [clojure.test :refer :all]
            [gorillalabs.tesla.stateful.appstate :as app-status]
            [com.stuartsierra.component :as c]
            [environ.core :as env]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [gorillalabs.tesla.util.test-utils :as u]
            [gorillalabs.tesla.tesla :as system]
            [gorillalabs.tesla.stateful.handler :as handler]
            [ring.mock.request :as mock]
            [gorillalabs.status :as s]
            [de.otto.status :as s]))









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
















(defn- serverless-system [runtime-config]
  (dissoc
    (system/base-system runtime-config)
    :server))

(deftest ^:unit should-have-system-status-for-runtime-config
  (u/with-started [system (serverless-system {:hostname "bar" :external-port "0123"})]
                  (let [status (:app-status system)
                        system-status (:system (app-status/status-response-body status))]
                    (is (= (:hostname system-status) "bar"))
                    (is (= (:port system-status) "0123"))
                    (is (not (nil? (:systemTime system-status)))))))

(deftest ^:unit should-sanitize-passwords
  (is (= (app-status/sanitize {:somerandomstuff                        "not-so-secret"
                               :somerandomstuff-passwd-somerandomstuff "secret"
                               :somerandomstuff-pwd-somerandomstuff    "secret"} ["passwd" "pwd"])
         {:somerandomstuff                        "not-so-secret"
          :somerandomstuff-passwd-somerandomstuff "******"
          :somerandomstuff-pwd-somerandomstuff    "******"})))

(defrecord MockStatusSource [response]
  c/Lifecycle
  (start [self]
    (app-status/register-status-fun (:app-status self) #(:response self))
    self)
  (stop [self]
    self))

(defn- mock-status-system [response]
  (assoc (serverless-system {})
    :mock-status
    (c/using (map->MockStatusSource {:response response}) [:app-status])))

(deftest ^:unit should-show-applicationstatus
  (u/with-started [started (mock-status-system {:mock {:status  :ok
                                                       :message "nevermind"}})]
                  (let [status (:app-status started)
                        page (app-status/status-response status)
                        _ (log/info page)
                        application-body (get (json/read-str (:body page)) "application")]
                    (testing "it shows OK as application status"
                      (is (= (get application-body "status")
                             "OK")))

                    (testing "it shows the substatus"
                      (is (= (get application-body "statusDetails")
                             {"mock" {"message" "nevermind" "status" "OK"}}))))))

(deftest ^:unit should-show-warning-as-application-status
  (u/with-started [started (mock-status-system {:mock {:status  :warning
                                                       :message "nevermind"}})]
                  (let [status (:app-status started)
                        page (app-status/status-response status)
                        applicationStatus (get (get (json/read-str (:body page)) "application") "status")]
                    (is (= applicationStatus "WARNING")))))


(deftest ^:integration should-serve-status-under-configured-url
  (testing "use the default url"
    (u/with-started [started (serverless-system {})]
                    (let [handlers (handler/handler (:handler started))]
                      (is (= (:status (handlers (mock/request :get "/status")))
                             200)))))

  (testing "use the configuration url"
    (u/with-started [started (serverless-system {:status {:path "/my-status"}})]
                    (let [handlers (handler/handler (:handler started))]
                      (is (= (:status (handlers (mock/request :get "/my-status")))
                             200)))))

  (testing "default should be overridden"
    (u/with-started [started (serverless-system {:status {:path "/my-status"}})]
                    (let [handlers (handler/handler (:handler started))]
                      (is (= (handlers (mock/request :get "/status"))
                             nil))))))

(deftest should-add-version-properties-to-status
  (testing "it should add the version properties"
    (u/with-started [started (serverless-system {})]
                    (let [handlers (handler/handler (:handler started))
                          request (mock/request :get "/status")
                          status-map (json/read-json (:body (handlers request)))]
                      (is (= (get-in status-map [:application :version]) "test.version"))
                      (is (= (get-in status-map [:application :git]) "test.githash"))))))

(deftest determine-status-strategy
  (testing "it should use strict stategy if none is configured"
    (let [config {:config {:status-aggregation nil}}]
      (is (= (app-status/aggregation-strategy config) s/strict-strategy))))

  (testing "it should use forgiving stategy if forgiving is configured"
    (let [config {:config {:status-aggregation "forgiving"}}]
      (is (= (app-status/aggregation-strategy config) s/forgiving-strategy))))

  (testing "it should use strict stategy if something else is configured"
    (let [config {:config {:status-aggregation "unknown"}}]
      (is (= (app-status/aggregation-strategy config) s/strict-strategy)))))