(ns keepia.cli.storage
  (:require [keepia.storage :as storage]
            [clojure.pprint :refer [pprint]])
  (:import (java.nio.file Path Paths)))

(defn- ^Path to-path [path]
  (Paths/get (str path) (make-array String 0)))

(defn put
  "store a file and return an id for it
   usage: keepia put FILE"
  [keptium file]
  (let [path (to-path file)]
    (println (storage/put (:storage keptium) path
                          {:filename (str (.getFileName path))}))))

(defn cat
  "print a blob on the standard output"
  [keptium id]
  (print (slurp (get (:storage keptium) id))))

(defn print-metadata
  "print a blob's metadata"
  [keptium id]
  (pprint (meta (get (:storage keptium) id))))

(def subcommands
  {"put" #'put
   "cat" #'cat
   "meta" #'print-metadata})
