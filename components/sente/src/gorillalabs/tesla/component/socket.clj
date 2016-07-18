(ns gorillalabs.tesla.component.socket
  (:require [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.http-kit :refer (get-sch-adapter)]
            [mount.core :as mnt]
            [clojure.tools.logging :as log]))

(declare socket)

(defn- start []
  (log/info "-> Starting sente socket ")
  (sente/make-channel-socket! (get-sch-adapter) {:packet :edn}))

(defn- stop [self]
  (log/info "<- Stopping sente socket."))

(mnt/defstate socket
              :start (start)
              :stop (stop socket))
