(ns keepia.system
  "The keepia digital library system"
  (:use (keepia web registry storage)))

(defn- close [^java.io.Closeable closeable]
  (.close closeable))

(defrecord Keepia [storage
                    registry
                    webserver]
  java.io.Closeable
  (close [this]
    (close webserver)
    (close registry)
    (close storage)))

(defn cli-system [options]
  (Keepia. (open-storage "/tmp/a")
           (open-registry "/tmp/a")
           nil))

(defn system [options]
  (let [keepia  (Keepia. 
                  (open-storage "/tmp/a")
                  (open-registry "/tmp/a")
                  nil)]
    (assoc keepia :webserver (open-webserver keepia (:web options)))))
