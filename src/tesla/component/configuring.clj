(ns tesla.component.configuring
  "This component is responsible for loading the configuration."
  (:require [com.stuartsierra.component :as component]
            [clojure.tools.logging :as log]
            [gorillalabs.config :as config]
            [environ.core :as environ]))


(defn config
  ([config key-path]
   (get-in (:config config) key-path))
  ([config key-path default]
   (get-in (:config config) key-path default)))


(defn load-config [self]
  (config/init (str (environ/env :system) "-" (environ/env :env))))

;; Load config on startup.
(defrecord Configuring [runtime-config load-config-fn]
  component/Lifecycle
  (start [self]
    (log/info "-> loading configuration.")
    (log/info runtime-config load-config-fn)
    (assoc self :config (merge (load-config-fn self) runtime-config)
                :version {:version "test.version"
                          :commit  "test.githash"}))        ;; TODO: Alter versions!

  (stop [self]
    (log/info "<- stopping configuration.")
    self))

(defn new-config [runtime-config & {:keys [load-config-fn] :or {load-config-fn load-config}}]
  (map->Configuring {:runtime-config runtime-config
                     :load-config-fn load-config-fn
                     }))
