(ns keepia.cli
  "Command-line interface"
  (:require [keepia.system :refer [system]])
  (:gen-class))

(defn -main [& args]
  (system {:web {:join? true}}))
