(ns gorillalabs.tesla.component.appstate
  (:require [mount.core :as mnt]
            [de.otto.status :as s]
            [clojure.data.json :as json :only [write-str]]
            [clojure.tools.logging :as log]
            [clojure.string :as str]
            [clj-time.local :as local-time]
            [metrics.timers :as timers]
            [gorillalabs.tesla.component.configuration :as config]))

(defmulti aggregation-strategy
          "Selects the status aggregation strategy based upon config."
          (fn [config]
            (keyword (config/config config [:status-aggregation]))))

(defmethod aggregation-strategy :forgiving [_]
  s/forgiving-strategy)

(defmethod aggregation-strategy :default [_]
  s/strict-strategy)








(defn- keyword-to-state [kw]
  (str/upper-case (name kw)))

(defn- sanitize-str [s]
  (apply str (repeat (count s) "*")))

(defn- sanitize-mapentry [checklist [k v]]
  {k (if (some true? (map #(.contains (name k) %) checklist))
       (sanitize-str v)
       v)})

(defn- sanitize [config checklist]
  (into {}
        (map (partial sanitize-mapentry checklist) config)))


(defn- to-json [compiled-state]
  (into {} (map
             (fn [[k v]]
               {k (update-in v [:status] keyword-to-state)})
             compiled-state)))





(defn- system-infos [config]
  {:systemTime (local-time/format-local-time (local-time/local-now) :date-time-no-ms)
   :hostname   (config/external-hostname config)
   :port       (config/external-port config)})




(defn current-state [appstate config]
  (let [extra-info {:name          (config/config config [:name])
                    :version       (config/config config [:version])
                    :git           (config/config config [:commit])
                    :configuration (sanitize (config/config config) ["pwd" "passwd"])}]
    (assoc
      (s/aggregate-status :application
                          (aggregation-strategy config)
                          @appstate
                          extra-info)
      :system (system-infos config))))









(defn- response-body [self config]
  (-> (current-state self config)
      (update-in [:application :statusDetails] to-json)
      (update-in [:application :status] keyword-to-state)))


(defn- response [self config]
  (timers/time! (:status-timer self)
                {:status  200
                 :headers {"Content-Type" "application/json"}
                 :body    (json/write-str (response-body self config))}))

(defn- deconj [seq item]
  (filterv (partial not= item) seq))


(defn register-state-fn [appstate state-fn]
  (swap! appstate conj state-fn))


(defn deregister-state-fn [appstate state-fn]
  (swap! appstate deconj state-fn))

#_(defn make-handler
    [self]
    (let [status-path (config/config (:config self) [:status :path] "/status")]
      (c/routes (c/GET status-path
                       []
                       (-> (c/GET status-path
                                  []
                                  (status-response self))
                           (ring-defaults/wrap-defaults
                             (assoc ring-defaults/site-defaults :session false
                                                                :cookies false
                                                                :static false
                                                                :proxy true)))))))


(defn- start []
  (log/info "-> start app-status")
  (atom []))


(mnt/defstate ^{:on-reload :noop}
              appstate
              :start (start)
              )