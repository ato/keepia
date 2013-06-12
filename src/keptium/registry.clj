(ns keptium.registry
  (:require [clojure.edn :as edn]
            [keptium.io :refer :all]))

(defrecord RegistryTx [declare retract])

(defprotocol Transactor
  (transact [registry tx]))

(defn- apply-tx [objects tx]
  (-> (apply dissoc objects (:retract tx))
      (into (:enact tx))))

(defrecord Registry [objects]
  Transactor
  (transact [this tx] (swap! objects apply-tx tx))
  java.io.Closeable
  (close [this]))

(defn open-registry [basedir]
  (Registry. (atom {})))

