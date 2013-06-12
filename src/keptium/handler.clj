(ns keptium.handler
  (:use compojure.core
        [clj-webjars :only [wrap-webjars]])
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure.java.io :as io]
            [clj-time.format :as timef]
            [clj-time.core :as time]))

(def ^:private http-time
  (-> (timef/formatter "EEE, dd MMM yyyy HH:mm:ss 'GMT'")
      (.withLocale java.util.Locale/US)))

(defn- blob-headers [metadata]  
  {"Content-Type"   (:content-type metadata)
   "Content-Length" (str (:size metadata))
   "Last-Modified"  (timef/unparse http-time (:timestamp metadata))})

(defn- get-blob [storage id]
  (let [blob (get storage id)]
    (if blob
      {:status  200
       :headers (blob-headers (meta blob))
       :body    (io/input-stream blob)}
      {:status 404
       :header {"Content-Type" "text/plain"}
       :body (str "Blob " id " not found\n")o})))

(defn app-routes [keptium]
  (routes 
   (GET "/" [] {:status 200, :body {:template "hello", :name "bob"}})
   (GET "/blobs/:id" [id] (get-blob (:storage keptium) id))
   (route/resources "/")
   (route/not-found "Not Found")))

(defn app-handler [keptium]
  (-> (handler/site (app-routes keptium))
      (wrap-webjars)))

;;(get (:storage user/system) "1")
;;((app-handler user/system) {:uri "/blobs/1", :request-method :get})
;;

