(ns kaibra.util.test-utils
  (:require [clojure.test :refer :all]
            [kaibra.stateful.configuring :as config]
            [environ.core :as env]
            [kaibra.system :refer [the-states]]
            [ring.mock.request :as mock]
            [mount.core :as mnt]))

(defn state-lookup [s]
  (if (keyword? s)
    (the-states s)
    s))

(defmacro start-and-stop-states [states & body]
  `(try
     (apply mnt/start (map state-lookup ~states))
     ~@body
     (finally
       (apply mnt/stop (map state-lookup ~states)))))

(defmacro with-states [states & args]
  (let [mocked-call (fn [s]
                         `(with-redefs-fn {~s ~(second args)}
                            #(with-states ~states ~@(rest (rest args)))))]
    (cond
      (and (= :runtime-config (first args)) (second args))
      (mocked-call #'config/runtime-config)

      (and (= :env (first args)) (second args))
      (mocked-call #'env/env)

      :default `(start-and-stop-states ~states ~@args))))

(defmacro with-started-system [& body]
  `(with-states (keys ~the-states) ~@body))

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