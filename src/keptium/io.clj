(ns keptium.io
  (:import (java.nio.file Path Paths)))

(defn ^Path to-path [path]
  (Paths/get (str path) (make-array String 0)))
