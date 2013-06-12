(ns keptium.webserver
  (:use compojure.core
        [keptium.handler :only [app-handler]]
        [keptium.handlebars :only [handlebars wrap-handlebars]]
        [ring.adapter.jetty :only [run-jetty]]
        [clj-webjars :only [wrap-webjars]]
        [ring.middleware.stacktrace :only [wrap-stacktrace]])
  (:require [compojure.handler :as handler]
            [compojure.route :as route])
  (:import org.eclipse.jetty.server.Server
           com.github.jknack.handlebars.Handlebars))

(defrecord WebServer [^Handlebars handlebars
                      ^Server jetty]
  java.io.Closeable
  (close [this] (.stop jetty)))

(def defaults {:port 3000, :join? false})

(defn open-webserver [keptium options]
  (let [handlebars (handlebars)
        options (merge defaults options)
        app (-> (app-handler keptium)
                (wrap-handlebars handlebars)
                (wrap-stacktrace))
        jetty (run-jetty app options)]
    (WebServer. handlebars jetty)))
