(ns tesla.component.configuring
  "This component is responsible for loading the configuration."
  (:require [com.stuartsierra.component :as component]
            [clojure.tools.logging :as log]
            [tesla.util.keyword :as kwutil]
            [gorillalabs.config :as config]
            [environ.core :as env :only [env]]))

(defn load-config []
  (config/init))

;; Load config on startup.
(defrecord Configuring [runtime-config]
  component/Lifecycle
  (start [self]
    (log/info "-> loading configuration.")
    (log/info runtime-config)
    (assoc self :config (merge (load-config) runtime-config)
                :version {:version "test.version"
                          :commit  "test.githash"}))        ;; TODO: Alter versions!

  (stop [self]
    (log/info "<- stopping configuration.")
    self))

(defn new-config [runtime-config] (map->Configuring {:runtime-config runtime-config}))


