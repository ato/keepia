(ns keepia.web.storage
  "Web interface to the keepia storage subsystem"
  (:require [compojure.core :refer [GET routes]]
            [clojure.java.io :as io]
            [clj-time.core   :as time]
            [clj-time.format :as timef]))

(def http-time
  (-> (timef/formatter "EEE, dd MMM yyyy HH:mm:ss 'GMT'")
      (.withLocale java.util.Locale/US)))

(defn blob-headers [metadata]
  {"Content-Type"   (:content-type metadata)
   "Content-Length" (str (:size metadata))
   "Last-Modified"  (timef/unparse http-time (:timestamp metadata))})

(defn get-blob [storage id]
  (let [blob (get storage id)]
    (if blob
      {:status  200
       :headers (blob-headers (meta blob))
       :body    (io/input-stream blob)}
      {:status 404
       :header {"Content-Type" "text/plain"}
       :body (str "Blob " id " not found\n")o})))

(defn subroutes [keepia]
  (let [storage (:storage keepia)]
    (routes
     (GET "/blobs/:id" [id] (get-blob storage id)))))
