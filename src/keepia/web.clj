(ns keepia.web
  "Web interface"
  (:use compojure.core
        [keepia.web.handlebars :only [handlebars wrap-handlebars]]
        [ring.adapter.jetty :only [run-jetty]]
        [clj-webjars :only [wrap-webjars]]
        [ring.middleware.stacktrace :only [wrap-stacktrace]])
  (:require keepia.web.storage
            [compojure.route :as route])
  (:import org.eclipse.jetty.server.Server
           com.github.jknack.handlebars.Handlebars))

(defn- web-routes [keepia]
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
        app (-> web-routes
                (wrap-handlebars handlebars)
                (wrap-stacktrace))
        jetty (run-jetty app options)]
    (WebServer. handlebars jetty)))
