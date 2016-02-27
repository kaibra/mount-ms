(ns test.kaibra.utils
  (:require [clojure.test :refer :all]
            [kaibra.stateful.configuring :as config]
            [environ.core :as env]
            [kaibra.system :refer [the-states]]
            [mount.core :as mnt]))

(defn- state-lookup [s]
  (if (keyword? s)
    (the-states s)
    s))

(defmacro start-and-stop-states [states & body]
  `(try
     (apply mnt/start (map ~state-lookup ~states))
     ~@body
     (finally
       (apply mnt/stop (map ~state-lookup ~states)))))

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

(defmacro with-extended-started-system [extended-states & body]
  `(with-states (concat (keys ~the-states) ~extended-states) ~@body))
