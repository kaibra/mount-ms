(ns gorillalabs.tesla.component.authorization
  (:require [mount.core :as mnt]
            [clojure.tools.logging :as log]
            [gorillalabs.tesla.component.configuration :as config]
            [gorillalabs.tesla.component.mongo :refer [mongo retrievePage retrieveItem]]
            [buddy.sign.jwe :as jwe]
            [clj-time.core :refer [hours from-now]]
            [buddy.auth.backends.token :as auth]
            [buddy.core.hash :as hash]
            ))


(def secret
  (delay (let [secret? (config/config config/configuration [:authorization :secret])]
           (when secret? (hash/sha256 secret?)))))

(def encryption (delay (config/config config/configuration [:authorization :encryption])))
(def authdata (delay (config/config config/configuration [:authorization :authdata])))

(defn- unauthorized [& _] {:status 403 :body {:message "Unauthorized. Please login first."}})

(defn authorize [username password]
  (let [valid? (some-> (deref authdata)
                       (get (keyword username))
                       (= password))]
    (when (and username password)
      (if valid?
        (let [claims {:user (keyword username)
                      :exp  (-> 3 hours from-now)}]
          (jwe/encrypt claims (deref secret) (deref encryption)))))))




(defn- start []
  (log/info "-> Starting authorization.")
  (if (and secret encryption authdata)
    (auth/jwe-backend {:secret               (deref secret)
                       :options              (deref encryption)
                       :unauthorized-handler unauthorized})
    (log/error "Autorization needs to be configured first")))

(defn- stop [self]
  (log/info "<- Stopping authorization")
  self)

(mnt/defstate authorization
              :start (start)
              :stop (stop authorization))
