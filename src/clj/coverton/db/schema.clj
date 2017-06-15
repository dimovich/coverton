(ns coverton.db.schema
  (:require [clojure.core.async :refer [<!!]]
            [datomic.client     :as    client]))


(def cover-schema [{:db/ident :cover/id
                    :db/valueType :db.type/uuid
                    :db/cardinality :db.cardinality/one
                    :db/doc "Cover id"}

                   {:db/ident :cover/url
                    :db/valueType :db.type/string
                    :db/cardinality :db.cardinality/one
                    :db/doc "Cover url"}

                   {:db/ident :cover/tags
                    :db/valueType :db.type/string
                    :db/cardinality :db.cardinality/many
                    :db/fulltext true
                    :db/doc "Cover tags"}])
