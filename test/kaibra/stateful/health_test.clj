(ns kaibra.stateful.health-test
  (:require
    [clojure.test :refer :all]
    [kaibra.stateful.health :as health]
    [ring.mock.request :as mock]
    [kaibra.util.test-utils :as u]))

(deftest ^:unit should-turn-unhealthy-when-locked
  (u/with-started-system
    (testing "it is still healthy when not yet locked"
      (let [response ((health/health-handler) (mock/request :get "/health"))]
        (are [key value] (= value (get response key))
                         :body "HEALTHY"
                         :status 200)))

    (testing "when locked, it is unhealthy"
      (health/lock-application)
      (let [response ((health/health-handler) (mock/request :get "/health"))]
        (are [key value] (= value (get response key))
                         :body "UNHEALTHY"
                         :status 503)))))

(deftest ^:integration should-serve-health-under-configured-url
  (testing "use the default url"
    (u/with-started-system
      (is (= (:body ((health/health-handler) (mock/request :get "/health")))
             "HEALTHY"))))

  (testing "use the configuration url"
    (u/with-started-system
      :runtime-config {:health-url "/my-health"}
      (is (= (:body ((health/health-handler) (mock/request :get "/my-health")))
             "HEALTHY")))))
