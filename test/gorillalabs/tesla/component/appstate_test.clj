(ns gorillalabs.tesla.component.appstate-test
  (:require [clojure.test :refer :all]
            [gorillalabs.tesla.component.appstate :as app-status]
            [environ.core :as env]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [gorillalabs.tesla.util.test-utils :as u]
            [gorillalabs.tesla.component.handler :as handler]
            [ring.mock.request :as mock]
            [de.otto.status :as s]
            [mount.core :as mnt]
            [gorillalabs.tesla :as tesla]
            [gorillalabs.tesla.component.appstate :as appstate]))


(deftest determine-status-strategy
  (let [aggregation-strategy #'appstate/aggregation-strategy]
    (testing "should use strict stategy if none is configured"
      (let [config {:appstate {:aggregation nil}}]
        (is (= s/strict-strategy (aggregation-strategy config)))))

    (testing "should use forgiving stategy if forgiving is configured"
      (let [config {:appstate {:aggregation :forgiving}}]
        (is (= s/forgiving-strategy (aggregation-strategy config)))))

    (testing "should use strict stategy if something else is configured"
      (let [config {:appstate {:aggregation :unknown}}]
        (is (= s/strict-strategy (aggregation-strategy config)))))))

(deftest test-appstate
  (let [create-appstate #'appstate/create-appstate]
    (testing "should work on empty appstate"
      (let [appstate (create-appstate)
            config {}

            result (appstate/current-state appstate config)
            ]
        (is (contains? (:application result) :name))
        (is (contains? (:application result) :version))
        (is (contains? (:application result) :git))
        (is (contains? (:application result) :message))
        (is (= (:status (:application result)) :ok))
        (is (= (:configuration (:application result)) config))


        (is (contains? (:system result) :systemTime))
        (is (contains? (:system result) :hostname))
        (is (contains? (:system result) :port))
        ))

    (testing "should work on appstate with state-fn"
      (let [state-fn-return-value {:test {:status :ok :message "msg"}}
            appstate (appstate/register-state-fn (create-appstate) (fn component-state-fn [] state-fn-return-value))
            config {}

            result (appstate/current-state appstate config)
            ]
        (is (= (:status (:application result)) :ok))
        (is (= (:statusDetails (:application result)) state-fn-return-value))
        ))

    (testing "should bubble-up warnings on appstate with state-fn"
      (let [state-fn-return-value {:test {:status :warning :message "msg"}}
            appstate (appstate/register-state-fn (create-appstate) (fn component-state-fn [] state-fn-return-value))
            config {}

            result (appstate/current-state appstate config)
            ]
        (is (= (:status (:application result)) :warning))
        (is (= (:statusDetails (:application result)) state-fn-return-value))
        ))
    ))





(deftest test-password-sanitization
  (let [create-appstate #'appstate/create-appstate]
    (testing "should sanitize passwords"
      (let [appstate (create-appstate)
            config {:somerandomstuff                        "not-so-secret"
                    :somerandomstuff-passwd-somerandomstuff "secret"
                    :somerandomstuff-pwd-somerandomstuff    "secret"}

            result (appstate/current-state appstate config)
            ]
        (is (= (:configuration (:application result)) {:somerandomstuff                        "not-so-secret"
                                                       :somerandomstuff-passwd-somerandomstuff "******"
                                                       :somerandomstuff-pwd-somerandomstuff    "******"}))
        ))))




(deftest should-handle-http-requests
  (let [create-appstate #'appstate/create-appstate
        create-handler #'appstate/create-handler
        ]
    (testing "it should return a http response"
      (let [appstate (create-appstate)
            config {}
            handler (create-handler appstate config)
            response (handler (mock/request :get "/status"))
            ]

        (is (map? response))
        (let [body-map (json/read-json (:body response))]
          (is (map? body-map))
          (is (= (:status (:application body-map)) "OK"))
          )))))