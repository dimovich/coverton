(ns coverton.db.schema)

(def cover-schema [{:db/ident :cover/id
                    :db/valueType :db.type/uuid
                    :db/cardinality :db.cardinality/one
                    :db/unique :db.unique/identity}
                   
                   {:db/ident :cover/image-url
                    :db/valueType :db.type/string
                    :db/cardinality :db.cardinality/one
                    :db/doc "Cover image url"}

                   {:db/ident :cover/tags
                    :db/valueType :db.type/string
                    :db/cardinality :db.cardinality/many
                    :db/fulltext true
                    :db/doc "Cover tags"}

                   {:db/ident :cover/marks
                    :db/valueType :db.type/ref
                    :db/cardinality :db.cardinality/many
                    :db/isComponent true
                    :db/doc "Cover artifacts"}])



(def mark-schema [{:db/ident :mark/id
                   :db/valueType :db.type/string
                   :db/unique :db.unique/identity
                   :db/cardinality :db.cardinality/one}
                  
                  {:db/ident :mark/font-name
                   :db/valueType :db.type/string
                   :db/cardinality :db.cardinality/one}
                  
                  {:db/ident :mark/font-size
                   :db/valueType :db.type/long
                   :db/cardinality :db.cardinality/one}

                  {:db/ident :mark/type
                   :db/valueType :db.type/ref
                   :db/cardinality :db.cardinality/one}
                  
                  {:db/ident :mark/pos
                   :db/valueType :db.type/float
                   :db/cardinality :db.cardinality/many}])



(def mark-types [:text :svg])


