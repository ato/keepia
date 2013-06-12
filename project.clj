(defproject keepia "0.1.0-SNAPSHOT"
  :description "digital library prototype"
  :url "http://github.com/ato/keepia"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.5"]
                 [clj-webjars "0.9.0"]
                 [org.webjars/bootstrap "2.3.1"]
                 [org.webjars/requirejs "2.1.5"]
                 [com.github.jknack/handlebars "0.12.0"]
                 [ring/ring-jetty-adapter "1.1.8"]
                 [ring/ring-devel "1.1.0"]
                 [clj-stacktrace "0.2.5"]
                 [clj-time "0.5.1"]]
  :profiles
  {:dev {:source-paths ["dev"]
         :dependencies [[ring-mock "0.1.5"]
                        [org.clojure/tools.namespace "0.2.3"]
                        [org.clojure/java.classpath "0.2.0"]]}})
