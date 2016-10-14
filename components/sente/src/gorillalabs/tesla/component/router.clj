( ns gorillalabs.tesla.component.router
  (:require [mount.core :as mnt]
            [clojure.tools.logging :as log]
            [clojure.core.async :as async :refer (<! <!! >! >!! put! chan go go-loop close!)]
            [gorillalabs.tesla.component.socket :as socket]
            [taoensso.sente :as sente]))

(declare router)

(defn- event-msg-handler
  "Wraps `-event-msg-handler` with logging, error catching, etc."
  [event-msg-channel]
  (go-loop [event-msg (<! event-msg-channel)]
           (when event-msg
             (let [{:keys [id event ?reply-fn]} event-msg]
               (go (if-let [event-fn (get @(:events router) id)]
                     (try
                       (event-fn event-msg)
                       (do (log/info "Unhandled event: %s" id)
                           (when ?reply-fn
                             (?reply-fn {:umatched-event-as-echoed-from-from-server event})))
                     (catch Exception e (log/error  "Caught exception: " (.getMessage e)))))
                   ))
             (recur (<! event-msg-channel)))))

(defn register [router-component id callback]
  (swap! (:events router-component) assoc id callback))

(defn deregister [router-component id]
  (swap! (:events router-component) dissoc id))

(defn- start []
  (log/info "-> Starting sente router")
  (let [event-msg-channel (chan)]
    (event-msg-handler event-msg-channel)
    {:event-msg-channel event-msg-channel :events (atom {}) :router (sente/start-server-chsk-router! (:ch-recv socket/socket) #(go (>! event-msg-channel %)))}))

(defn- stop [self]
  (log/info "<- Stopping sente router.")
  (when (:router self) ((:router self)))
  (when (:event-msg-channel self) (close! (:event-msg-channel self))))

(mnt/defstate router
  :start (start)
  :stop (stop router))
