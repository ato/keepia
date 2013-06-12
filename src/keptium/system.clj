(ns keptium.system
  (:require keptium.handler)
  (:use (keptium webserver registry storage)))

(defn- close [^java.io.Closeable closeable]
  (.close closeable))

(defrecord Keptium [storage
                    registry
                    webserver]
  java.io.Closeable
  (close [this]
    (close webserver)
    (close registry)
    (close storage)))

(defn system []  
  (let [keptium  (Keptium. 
                  (open-storage "/tmp/a")
                  (open-registry "/tmp/a")
                  nil)]
    (assoc keptium :webserver (open-webserver keptium {}))))
