(ns gorillalabs.tesla.component.httpkit
  (:require [bidi.ring :as br]
            [org.httpkit.server :as httpkit]
            [clojure.tools.logging :as log]
            [gorillalabs.tesla.component.configuration :as config]
            [gorillalabs.tesla.component.handler :as handler]
            [mount.core :as mnt]))

;; TODO: Sorry, much is broken here
;; - Handler (use the current handlers even if handlers changed after http-kit started. Otherwise, you'll loose the ability to change stuff on the fly and you'll have to start http-kit after your handler-using-components. [TODO]

(defmacro condasit->
  "A mixture of cond-> and as-> allowing more flexibility in the test and step forms, also binding `it` to the result of the cond predicate."
  [expr name & clauses]
  (assert (even? (count clauses)))
  (let [pstep (fn [[test step]] `(if-let [~'it ~test] ~step ~name))]
    `(let [~name ~expr
           ~@(interleave (repeat name) (map pstep (partition 2 clauses)))]
       ~name)))

(def default-port 3000)

(defn server-config [config]
  (condasit-> {:port (get-in config [:httpkit :port] default-port)
               :ip   (get-in config [:httpkit :bind] "0.0.0.0")}
              server-conf

              (get-in config [:httpkit :thread])
              (assoc server-conf :thread it)

              (get-in config [:httpkit :queue-size])
              (assoc server-conf :queue-size it)

              (get-in config [:httpkit :max-body])
              (assoc server-conf :max-body it)

              (get-in config [:httpkit :max-line])
              (assoc server-conf :max-line it)))


(defn- start []
  (log/info "-> starting httpkit")
  (let [server-config (server-config config/configuration)
        routes        ["" @handler/handler]
        _             (log/info "Starting httpkit with port " (server-config :port) " and bind " (server-config :ip) ":" routes)
        server        (httpkit/run-server (bidi.ring/make-handler routes) server-config)]
    server))

(defn- stop [server]
  (let [timeout (config/config config/configuration [:httpkit-timeout] 100)]
    (if server
      (do
        (log/info "<- stopping httpkit with timeout:" timeout "ms")
        (server :timeout timeout))
      (log/info "<- stopping httpkit"))))


(mnt/defstate httpkit
              :start (start)
              :stop (stop httpkit))
