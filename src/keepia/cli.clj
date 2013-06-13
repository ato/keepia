(ns keepia.cli
  "Command-line interface"
  (:require [keepia.system :refer [system cli-system]]
            [keepia.cli.storage])
  (:gen-class))

(defn subcommands []
  (merge keepia.cli.storage/subcommands))

(defn usage [keptium & args]
  (println "wat?"))

(comment
 (defn web [keptium & args]
   (system {:web {:join? true}})))

(defn -main [& args]
  (let [command (first args)
        f (get (subcommands) command usage)]
    (apply f (cli-system {}) (rest args))))
