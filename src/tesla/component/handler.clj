(ns tesla.component.handler
  (:require [com.stuartsierra.component :as component]
            [clojure.tools.logging :as log]
            [compojure.core :as c]))

;; TODO: Push middleware down to the routes added here!
;; See e.g. http://stackoverflow.com/questions/10822033/compojure-routes-with-different-middleware
;; TODO: This is necessary if you want to support different parts in the same Service (API, UI, Status/Health-Checks). They might require different middleware. See ring-defaults library ().

(defprotocol HandlerContainer
  (register-handler [self routes])
  (handler [self]))

(defrecord Handler []
  component/Lifecycle
  (start [self]
    (log/info "-> starting Handler")
    (assoc self :the-handlers (atom [])))
  (stop [self]
    (log/info "<- stopping Handler")
    self)
  HandlerContainer
  (register-handler [self handler] (swap! (:the-handlers self) #(conj % handler)))
  (handler [self]
    (let [handlers (:the-handlers self)]
      (apply c/routes @handlers))))

(defn new-handler []
  (map->Handler {}))
