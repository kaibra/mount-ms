(ns gorillalabs.tesla.component.authorization
  (:require [mount.core :as mnt]
            [clojure.tools.logging :as log]
            [gorillalabs.tesla.component.configuration :as config]
            [buddy.sign.jwe :as jwe]
            [clj-time.core :refer [hours from-now]]
            [buddy.auth.backends.token :as auth]
            [buddy.core.hash :as hash]
            ))


(declare authorization)


(defn- get-config [key config]
  (config/config config [:authorization key]))

(defn- secret [config]
  (let [secret? (config/config config [:authorization :secret])]
    (when secret? (hash/sha256 secret?))))

(def options
  (partial get-config :options))

(def authdata
  (partial get-config :authdata))

(defn- unauthorized [& _] {:status 403 :body {:message "Unauthorized. Please login first."}})

(defn authorize [username password]
  (let [valid? (some-> (:authdata authorization)
                       (get (keyword username))
                       (= password))]
    (when (and username password)
      (if valid?
        (let [claims {:user (keyword username)
                      :exp  (-> 3 hours from-now)}]
          (jwe/encrypt claims (:secret authorization) (:options authorization)))))))

(defn- start []
  (log/info "-> Starting authorization.")
  (let [config config/configuration
        state {:secret               (secret config)
               :options              (options config)
               :authdata             (authdata config)
               :unauthorized-handler unauthorized}]
    (assoc state :jwe-backend (auth/jwe-backend state))))

(defn- stop [self]
  (log/info "<- Stopping authorization")
  self)

(mnt/defstate authorization
              :start (start)
              :stop (stop authorization))
