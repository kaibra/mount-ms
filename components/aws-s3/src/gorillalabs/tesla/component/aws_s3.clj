(ns gorillalabs.tesla.component.aws-s3
  (:require [gorillalabs.tesla.component.configuration :as config]
            [mount.core :as mnt]
            [aws.sdk.s3 :as s3]
            [clojure.tools.logging :as log]))


;; TODO add support for multiple stores

(declare store)

(defn- cfg [item]
  (config/config config/configuration [:s3-store item]))

(defn init []
  (let [bucket-names (cfg :create-buckets)
        credentials (cfg :credentials)]
    (log/info "Creating configured buckets")
    (doseq [bucket-name bucket-names]
      (when-not (some #(= % bucket-name) (s3/list-buckets credentials))
        (log/infof "Creating bucket %s in object storage." bucket-name)
        (s3/create-bucket credentials bucket-name)))
    (atom {:credentials credentials})))



(defn store-import [bucket-name store-object]
  "Stores the object in the object store and returns the generated key. Object can either be an InputStream, a string or a file."
  (let [key (.toString (java.util.UUID/randomUUID))
        e-tag (.getETag (s3/put-object (:credentials @store) bucket-name key store-object))]
    (log/infof "Uploaded object with key %s and etag %s." key e-tag)
    key))

;; TODO error handling
(defn with-object-stream [bucket-name object-key f]
  "Calls the supplied function with the input-stream of the object referenced by the supplied key"
  (with-open [in-stream (:content (s3/get-object (:credentials @store) bucket-name object-key))]
    (f in-stream)))

(mnt/defstate store
  :start (init))

















