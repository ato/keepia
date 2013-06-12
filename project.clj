(defproject keptium "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.5"]
                 [clj-webjars "0.9.0"]
                 [org.webjars/bootstrap "2.3.1"]
                 [org.webjars/requirejs "2.1.5"]
                 [com.github.jknack/handlebars "0.12.0"]
                 [org.clojure/tools.reader "0.7.4"]
                 [ring/ring-jetty-adapter "1.1.8"]
                 [ring/ring-devel "1.1.0"]
                 [com.datomic/datomic-free "0.8.3993"]
                 [clj-stacktrace "0.2.5"]
                 [clj-time "0.5.1"]]
  :plugins [[lein-ring "0.8.5"]]
  :ring {:handler keptium.handler/app
         :nrepl {:start? true :port 7000}}
  :profiles
  {:dev {:source-paths ["dev"]
         :dependencies [[ring-mock "0.1.5"]
                        [org.clojure/tools.namespace "0.2.3"]
                        [org.clojure/java.classpath "0.2.0"]]}})
