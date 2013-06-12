(ns keptium.storage
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            clj-time.coerce)
  (:use keptium.io)
  (:import (java.nio.file Path Paths Files StandardOpenOption LinkOption)
           (java.nio.channels Channels WritableByteChannel)
           (java.io PushbackReader)))

(def ^:private open-options
  {:read StandardOpenOption/READ
   :write StandardOpenOption/WRITE
   :create StandardOpenOption/CREATE
   :append StandardOpenOption/APPEND})

(defn- open-option-array [opts]
  (into-array (remove nil? (map open-options opts))))

(def ^:private read-only (open-option-array [:read]))
(def ^:private create-only (open-option-array [:write :create]))
(def ^:private follow-symlinks (make-array LinkOption 0))

(defprotocol Transactional
  (begin [x]))

(defprotocol Transaction
  (rollback [tx])
  (commit [tx]))

(defprotocol Putbox
  (put [box output metadata]))

(defprotocol ChannelOutput
  (write-to-channel [x ^WritableByteChannel channel]))

(defprotocol IdGenerator
  (genid [gen]))

(extend-protocol ChannelOutput
  String
  (write-to-channel [s ^WritableByteChannel channel]
    (with-open [writer (Channels/newWriter channel "UTF-8")]
      (.write writer s))))

(defn- put-file [^Path basedir id contents]
  (let [path (.resolve basedir id)]
   (with-open [channel (Files/newByteChannel path create-only)]
     (write-to-channel contents channel))))

(deftype NaiveTransaction [store]
  Putbox
  (put [this output metadata]
    (let [id (str (genid store))
          basedir (.basedir store)]
      (put-file basedir id output)
      (put-file basedir (str id ".meta") (prn-str metadata))
      id)))

(defn- last-modified [^Path path]
  (-> (Files/getLastModifiedTime path follow-symlinks)
      (.toMillis)
      (clj-time.coerce/from-long)))

(deftype NaiveBlob [^Path path ^Path meta-path]
  clojure.lang.IMeta
    (meta [this] 
      (with-open [reader (PushbackReader. (io/reader (.toFile meta-path)))]
       (-> (edn/read reader)
           (assoc :size (Files/size path)
                  :timestamp (last-modified path)))))
  io/IOFactory
    (io/make-reader [this opts]
      (io/make-reader (io/make-input-stream this opts) opts))
    (io/make-input-stream [this opts]
      (Files/newInputStream path read-only)))

(deftype NaiveStorage [^Path basedir last-id]
  java.io.Closeable
    (close [this])
  clojure.lang.ILookup
    (valAt [this id] (.valAt this id nil))
    (valAt [this id not-found]
      (let [path (.resolve basedir id)
            meta-path (.resolve basedir (str id ".meta"))]
        (if (Files/exists path follow-symlinks)
          (NaiveBlob. path meta-path)
          not-found)))
  Transactional
    (begin [this] (NaiveTransaction. this))
  IdGenerator
    (genid [this] (str (swap! last-id inc))))

(defn open-storage [basedir]
  (NaiveStorage. (to-path basedir) (atom 0)))

;; (meta (get (open-storage "/tmp") "1"))

