(ns kaibra.transition.health-test
  (:require [clojure.test :refer :all]
            [kaibra.transition.health :as thealth]
            [kaibra.util.test-utils :as u]
            [ring.mock.request :as mock]))

(deftest ^:unit should-turn-unhealthy-when-locked
  (u/with-started-system
    (testing "it is still healthy when not yet locked"
      (let [response ((thealth/health-handler) (mock/request :get "/health"))]
        (are [key value] (= value (get response key))
                         :body "HEALTHY"
                         :status 200)))

    (testing "when locked, it is unhealthy"
      (thealth/lock-application)
      (let [response ((thealth/health-handler) (mock/request :get "/health"))]
        (are [key value] (= value (get response key))
                         :body "UNHEALTHY"
                         :status 503)))))

(deftest ^:integration should-serve-health-under-configured-url
  (testing "use the default url"
    (u/with-started-system
      (is (= (:body ((thealth/health-handler) (mock/request :get "/health")))
             "HEALTHY"))))

  (testing "use the configuration url"
    (u/with-runtime-config
      {:health-url "/my-health"}
      (u/with-started-system
        (is (= (:body ((thealth/health-handler) (mock/request :get "/my-health")))
               "HEALTHY"))))))
