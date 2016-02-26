(ns kaibra.stateful.app-status
  (:require [clojure.tools.logging :as log]
            [de.otto.status :as s]
            [mount.core :refer [defstate]]
            [clj-time.local :as local-time]
            [kaibra.stateful.configuring :as conf]
            [clojure.string :as str]
            [metrics.timers :as timers]
            [clojure.data.json :as json]
            [compojure.core :as c]))

(defn- aggregation-strategy []
  (if (= (conf/conf-prop :status-aggregation) "forgiving")
    s/forgiving-strategy
    s/strict-strategy))

(defn- start-app-status []
  (log/info "-> start app-status")
  {:status-aggregation (aggregation-strategy)
   :status-functions   (atom [])})

(defstate app-status :start (start-app-status))

(defn register-status-fun [fun]
  (swap! (:status-functions app-status) #(conj % fun)))

(defn- system-infos []
  {:systemTime (local-time/format-local-time (local-time/local-now) :date-time-no-ms)
   :hostname   (conf/external-hostname)
   :port       (conf/external-port)})

(defn- sanitize-str [s]
  (apply str (repeat (count s) "*")))

(defn- sanitize-mapentry [checklist [k v]]
  {k (if (some true? (map #(.contains (name k) %) checklist))
       (sanitize-str v)
       v)})

(defn- sanitize [checklist]
  (into {}
        (map (partial sanitize-mapentry checklist) (:config (conf/the-conf)))))

(defn- keyword-to-status [kw]
  (str/upper-case (name kw)))

(defn- status-details-to-json [details]
  (into {} (map
             (fn [[k v]]
               {k (update-in v [:status] keyword-to-status)})
             details)))

(defn- create-complete-status []
  (let [aggregate-strategy (:status-aggregation app-status)
        extra-info {:name          (conf/version-prop :name)
                    :version       (conf/version-prop :version)
                    :git           (conf/version-prop :commit)
                    :configuration (sanitize ["passwd" "pwd"])}]
    (assoc
      (s/aggregate-status :application
                          aggregate-strategy
                          @(:status-functions app-status)
                          extra-info)
      :system (system-infos))))

(defn- status-response-body []
  (-> (create-complete-status)
      (update-in [:application :statusDetails] status-details-to-json)
      (update-in [:application :status] keyword-to-status)))

(defn- status-response []
  (timers/time! (timers/timer ["status"])
                {:status  200
                 :headers {"Content-Type" "application/json"}
                 :body    (json/write-str (status-response-body))}))

(defn app-status-handler []
  (let [status-path (or (conf/conf-prop :status-url) "/status")]
    (c/routes (c/GET status-path [] (status-response)))))

