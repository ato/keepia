(ns keepia.web
  "Web interface"
  (:use compojure.core
        [keepia.handler :only [app-handler]]
        [keepia.handlebars :only [handlebars wrap-handlebars]]
        [ring.adapter.jetty :only [run-jetty]]
        [clj-webjars :only [wrap-webjars]]
        [ring.middleware.stacktrace :only [wrap-stacktrace]])
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            keepia.web.storage)
  (:import org.eclipse.jetty.server.Server
           com.github.jknack.handlebars.Handlebars))

(defn- routes [keepia]
  (routes 
   (GET "/" [] {:status 200, :body {:template "hello", :name "bob"}})
   (keepia.web.storage/subroutes keepia)
   (route/resources "/")
   (route/not-found "Not Found")))

(defrecord WebServer [^Handlebars handlebars
                      ^Server jetty]
  java.io.Closeable
  (close [this] (.stop jetty)))

(def ^:private defaults {:port 3000, :join? false})

(defn open-webserver [keepia options]
  (let [handlebars (handlebars)
        options (merge defaults options)
        app (-> (app-handler keepia)
                (wrap-handlebars handlebars)
                (wrap-stacktrace))
        jetty (run-jetty app options)]
    (WebServer. handlebars jetty)))
