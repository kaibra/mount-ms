(ns kaibra.util.test-utils
  (:require [clojure.test :refer :all]
            [kaibra.stateful.app-status :as app-status]
            [kaibra.stateful.configuring :as config]
            [ring.mock.request :as mock]
            [mount.core :as mnt]))

(defmacro with-started-system [& body]
  `(try
     (mnt/start)
     ~@body
     (finally
       (mnt/stop))))

(defmacro with-started-system-states [states & body]
  `(try
     (apply mnt/start ~states)
     ~@body
     (finally
       (apply mnt/stop ~states))))

(defmacro with-app-status [& body]
  `(with-started-system-states [#'app-status/app-status #'config/config] ~@body))

(defmacro with-config [& body]
  `(with-started-system-states [#'config/config] ~@body))

(defmacro with-runtime-config [runtime-conf & body]
  `(with-redefs [config/runtime-config (constantly ~runtime-conf)]
     ~@body))

(defn merged-map-entry [request args k]
  (let [merged (merge (k request) (k args))]
    {k merged}))

(defn mock-request [method url args]
  (let [request (mock/request method url)
        all-keys (keys args)
        new-input (map (partial merged-map-entry request args) all-keys)]
    (merge request (into {} new-input))))

(deftest testing-the-mock-request
  (testing "should create mock-request"
    (is (= (mock-request :get "url" {})
           {:headers        {"host" "localhost"}
            :query-string   nil
            :remote-addr    "localhost"
            :request-method :get
            :scheme         :http
            :server-name    "localhost"
            :server-port    80
            :uri            "url"})))
  (testing "should create mock-request"
    (is (= (mock-request :get "url" {:headers {"content-type" "application/json"}})
           {:headers        {"host"         "localhost"
                             "content-type" "application/json"}
            :query-string   nil
            :remote-addr    "localhost"
            :request-method :get
            :scheme         :http
            :server-name    "localhost"
            :server-port    80
            :uri            "url"}))))