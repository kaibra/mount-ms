( ns gorillalabs.tesla.component.router
  (:require [mount.core :as mnt]
            [clojure.tools.logging :as log]
            [gorillalabs.tesla.component.socket :as socket]
            [taoensso.sente :as sente]))

(declare router)

(defn- event-msg-handler
  "Wraps `-event-msg-handler` with logging, error catching, etc."
  [{:as ev-msg :keys [id ?data event ?reply-fn]}]

  (if-let [event-fn (get @(:events router) id)]
    (event-fn ev-msg)
    (do (log/info "Unhandled event: %s" id)
        (when ?reply-fn
          (?reply-fn {:umatched-event-as-echoed-from-from-server event})))))

(defn register [router-component id callback]
  (swap! (:events router-component) assoc id callback))

(defn deregister [router-component id]
  (swap! router-component dissoc id))

(defn- start []
  (log/info "-> Starting sente router")
  {:events (atom {}) :router (sente/start-server-chsk-router!  (:ch-recv socket/socket) event-msg-handler) })

(defn- stop [self]
  (log/info "<- Stopping sente router.")
  (when (:router self) ((:router self))))

(mnt/defstate router
  :start (start)
  :stop (stop router))
