(ns tesla.component.configuring-test
  (:require [clojure.test :refer :all]
            [tesla.component.configuring :as configuring]
            [com.stuartsierra.component :as component]
            [clojure.java.io :as io]
            [tesla.util.test-utils :as u]
            [environ.core :as env]))

(defn- test-system [rt-conf]
  (-> (component/system-map
        :conf (configuring/new-config rt-conf))))





#_ (deftest ^:unit should-read-propper-keywords
  (testing "should read the cache-dir as propper sanatized keyowrd from config"
    (let [loaded-properties (configuring/load-config)]
      (is (not (empty? (:cache-dir loaded-properties))))))

  (testing "should read the metering-reporter as propper sanatized keyowrd from config"
    (let [loaded-properties (configuring/load-config)]
      (is (not (empty? (:metering-reporter loaded-properties)))))))



