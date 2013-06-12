(ns keepia.io
  "I/O utilities"
  (:import (java.nio.file Path Paths)))

(defn ^Path to-path [path]
  (Paths/get (str path) (make-array String 0)))
