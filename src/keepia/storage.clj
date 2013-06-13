(ns keepia.storage
  "Object storage subsystem

   Stores and retrieves fixed length streams of bytes called blobs.

   Blobs may have arbitrary metadata associated with them. The storage
   subsystem itself will add some metadata entries:

       :size        number of bytes in the body of the blob
       :timestamp   time the blob was written"
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            clj-time.coerce)
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
        (.write writer s)))
  Path
    (write-to-channel [this ^WritableByteChannel out]
      (with-open [in (Files/newByteChannel this read-only)]
        (.transferTo in 0 Long/MAX_VALUE out))))

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
    (genid [this] (str (swap! last-id inc)))
  Putbox
    (put [this output metadata]
      (put (begin this) output metadata)))

(defn- ^Path to-path [path]
  (Paths/get (str path) (make-array String 0)))

(defn open-storage [basedir]
  (NaiveStorage. (to-path basedir) (atom 0)))

