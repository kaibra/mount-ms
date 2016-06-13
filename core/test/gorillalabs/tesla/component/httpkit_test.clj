(ns gorillalabs.tesla.component.httpkit-test
  (:require [clojure.test :refer :all]
            [gorillalabs.tesla.component.httpkit :refer :all]
            [gorillalabs.tesla.component.configuration :as config]))


(deftest test-server-config
  (testing "testing cornercase credentials"
    (is (= (:port (server-config {})) 3000))
    (is (= (:port (server-config {:httpkit {:port 3002}})) 3002))
    ))
