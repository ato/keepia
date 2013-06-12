(ns keepia.system
  "The keepia digital library system"
  (:require keepia.handler)
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

(defn system []  
  (let [keepia  (Keepia. 
                  (open-storage "/tmp/a")
                  (open-registry "/tmp/a")
                  nil)]
    (assoc keepia :webserver (open-webserver keepia {}))))
