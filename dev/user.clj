(ns user
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.pprint :refer (pprint)]
            [clojure.repl :refer :all]
            [clojure.test :as test]
            [clojure.tools.namespace.repl :refer (refresh refresh-all)]
            [keepia.system :as system]))

(def system nil)

(defn start
  "Constructs the current development system."
  []
  (alter-var-root #'system (constantly (system/system))))

(defn stop
  "Shuts down and destroys the current development system."
  []
  (alter-var-root #'system
    (fn [s] (when s (.close s)))))

(defn reset []
  (stop)
  (refresh :after 'user/start))
