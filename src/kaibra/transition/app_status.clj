(ns kaibra.transition.app-status
  (:require [compojure.core :as c]
            [clojure.data.json :as json :only [write-str]]
            [clojure.tools.logging :as log]
            [clojure.string :as str]
            [clj-time.local :as local-time]
            [environ.core :as env]
            [kaibra.stateful.app-status :refer [app-status]]
            [kaibra.transition.configuring :as tconf]
            [de.otto.status :as s]
            [mount.core :refer [defstate]]
            [metrics.timers :as timers]))

(defn register-status-fun [fun]
  (swap! (:status-functions app-status) #(conj % fun)))

(defn- system-infos []
  {:systemTime (local-time/format-local-time (local-time/local-now) :date-time-no-ms)
   :hostname   (tconf/external-hostname)
   :port       (tconf/external-port)})

(defn- sanitize-str [s]
  (apply str (repeat (count s) "*")))

(defn- sanitize-mapentry [checklist [k v]]
  {k (if (some true? (map #(.contains (name k) %) checklist))
       (sanitize-str v)
       v)})

(defn- sanitize [checklist]
  (into {}
        (map (partial sanitize-mapentry checklist) (:config (tconf/the-conf)))))

(defn- keyword-to-status [kw]
  (str/upper-case (name kw)))

(defn- status-details-to-json [details]
  (into {} (map
             (fn [[k v]]
               {k (update-in v [:status] keyword-to-status)})
             details)))

(defn- create-complete-status []
  (let [aggregate-strategy (:status-aggregation app-status)
        extra-info {:name          (tconf/version-prop :name)
                    :version       (tconf/version-prop :version)
                    :git           (tconf/version-prop :commit)
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
  (let [status-path (or (tconf/conf-prop :status-url) "/status")]
    (c/routes (c/GET status-path [] (status-response)))))
