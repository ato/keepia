(ns keepia.registry
  "Object registry subsystem

   Maintains a mapping between object ids and storage ids."
  (:require [clojure.edn :as edn]))

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

