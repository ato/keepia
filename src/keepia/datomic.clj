(ns keepia.datomic
  (:use [datomic.api :only [q db] :as d]))

(def uri "datomic:mem://seattle")

(d/create-database uri)

;; connect to database
(def conn (d/connect uri))

(def schema-tx 
  [{:db/id #db/id[:db.part/db]
    :db/ident :object/bibid
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "The catalogue record for this object"
    :db.install/_attribute :db.part/db}

   ;; page
   {:db/id #db/id[:db.part/db]
    :db/ident :page/index
    :db/valueType :db.type/long
    :db/cardinality :db.cardinality/one
    :db/doc ""
    :db.install/_attribute :db.part/db}

   {:db/id #db/id[:db.part/db]
    :db/ident :page/index
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "The catalogue record for this object"
    :db.install/_attribute :db.part/db}])
