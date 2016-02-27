(ns kaibra.stateful.configuring
  "This component is responsible for loading the configuration."
  (:require [mount.core :refer [defstate]]
            [clojurewerkz.propertied.properties :as p]
            [clojure.java.io :as io]
            [kaibra.util.keyword :as kwutil]
            [environ.core :as env :only [env]]
            [kaibra.util.env_var_reader :only [read-env-var]]
            [clojure.tools.logging :as log])
  (:import (java.io PushbackReader)))

(defn- load-properties-from-resource [resource]
  (kwutil/sanitize-keywords
    (p/properties->map
      (p/load-from resource) false)))

(defn- load-properties [name & [type]]
  (cond
    (and (= :properties type) (io/resource name)) (load-properties-from-resource (io/resource name))
    (and (= :file type) (.exists (io/file name))) (load-properties-from-resource (io/file name))))

(defn- load-config-from-property-files []
  (let [defaults (load-properties "default.properties" :properties)
        config (load-properties (or (:config-file env/env) "application.properties") :file)
        local (load-properties "local.properties" :properties)]
    (merge defaults config local env/env)))

(defn- load-edn [name]
  (when-let [resource (io/resource name)]
    (-> resource
        (io/reader)
        (PushbackReader.)
        (read))))

(defn- load-config-from-edn-files []
  (let [defaults (load-edn "default.edn")
        config (load-edn (or (:config-file env/env) "application.edn"))
        local (load-edn "local.edn")]
    (merge defaults config local)))

(defn- load-and-merge [runtime-config]
  (if-not (:property-file-preferred runtime-config)
    (merge (load-config-from-edn-files) runtime-config)
    (merge (load-config-from-property-files) runtime-config)))

(def runtime-config {})

(defn start-configuring []
  (log/info "-> start config")
  (let [conf {:config  (load-and-merge runtime-config)
              :version (load-properties "version.properties" :properties)}]
    conf))

(defstate config :start (start-configuring))

(defn conf-prop [& path]
  (get-in config (concat [:config] path)))

(defn the-conf []
  config)

(defn version-prop [& path]
  (get-in config (concat [:version] path)))

;; The hostname and port visble from the outside are different for
;; different environments.
;; These methods default to Marathon defaults.
(defn external-hostname []
  (or (conf-prop :host-name)
      (:host env/env) (:host-name env/env) (:hostname env/env)
      "localhost"))

;; see above
(defn external-port []
  (or (conf-prop :server-port)
      (:port0 env/env) (:host-port env/env) (:server-port env/env)))
