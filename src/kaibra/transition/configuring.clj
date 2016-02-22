(ns kaibra.transition.configuring
  (:require [kaibra.stateful.configuring :refer [config]]
            [environ.core :as env :only [env]]
            [kaibra.util.env_var_reader :only [read-env-var]]
            [clojure.tools.logging :as log]))

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

