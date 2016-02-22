(ns kaibra.stateful.app-status-test
  (:require [clojure.test :refer :all]
            [kaibra.stateful.app-status :as app-status]
            [kaibra.util.test-utils :as u]
            [de.otto.status :as s]))

(def aggregation-strategy #'app-status/aggregation-strategy)

(deftest determine-status-strategy
  (testing "it should use strict stategy if none is configured"
    (u/with-runtime-config
      {:status-aggregation nil}
      (u/with-config
        (is (= s/strict-strategy (aggregation-strategy))))))

  (testing "it should use forgiving stategy if forgiving is configured"
    (u/with-runtime-config
      {:status-aggregation "forgiving"}
      (u/with-config
        (is (= s/forgiving-strategy (aggregation-strategy))))))

  (testing "it should use strict stategy if something else is configured"
    (u/with-runtime-config
      {:status-aggregation "unknown"}
      (u/with-config
        (is (= s/strict-strategy (aggregation-strategy)))))))
