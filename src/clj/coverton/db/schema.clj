(ns coverton.db.schema
  (:require [clojure.core.async :refer [<!!]]
            [datomic.client     :as    client]))


(def cover-schema [{:db/ident :cover/image-url
                    :db/valueType :db.type/string
                    :db/cardinality :db.cardinality/one
                    :db/doc "Cover image url"}

                   {:db/ident :cover/tags
                    :db/valueType :db.type/string
                    :db/cardinality :db.cardinality/many
                    :db/fulltext true
                    :db/doc "Cover tags"}

                   {:db/ident :cover/artifacts
                    :db/valueType :db.type/ref
                    :db/cardinality :db.cardinality/many
                    :db/doc "Cover artifacts"}])



;; look up some examples

(def artifact-schema [])

{:mark/font-name "Gotha"
 :mark/font-size "10"
 :mark/pos [453/34 ]} ;;can we use rationals to have lossless conversion?
