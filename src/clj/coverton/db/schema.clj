(ns coverton.db.schema)

(def cover-schema [{:db/ident :cover/image-url
                    :db/valueType :db.type/string
                    :db/cardinality :db.cardinality/one
                    :db/doc "Cover image url"}

                   {:db/ident :cover/tags
                    :db/valueType :db.type/string
                    :db/cardinality :db.cardinality/many
                    :db/fulltext true
                    :db/doc "Cover tags"}

                   {:db/ident :cover/size
                    :db/valueType :db.type/long
                    :db/cardinality :db.cardinality/many
                    :db/doc "Cover size [width height"}

                   {:db/ident :cover/marks
                    :db/valueType :db.type/ref
                    :db/cardinality :db.cardinality/many
                    :db/isComponent true
                    :db/doc "Cover artifacts"}])



(def mark-schema [{:db/ident :mark/font-name
                   :db/valueType :db.type/string
                   :db/cardinality :db.cardinality/one}
                  
                  {:db/ident :mark/font-size
                   :db/valueType :db.type/long
                   :db/cardinality :db.cardinality/one}

                  {:db/ident :mark/url
                   :db/valueType :db.type/string
                   :db/cardinality :db.cardinality/one}

                  {:db/ident :mark/type
                   :db/valueType :db.type/keyword
                   :db/cardinality :db.cardinality/one}

                  {:db/ident :mark/text
                   :db/valueType :db.type/string
                   :db/cardinality :db.cardinality/one}
                  
                  {:db/ident :mark/pos
                   :db/valueType :db.type/long
                   :db/cardinality :db.cardinality/many}])



(def sample-data [{:cover/image-url "assets/img/coverton.jpg"
                   :cover/tags ["sea" "boat" "depth" "children"]
                   :cover/size [400 400]
                   :cover/marks  [{:mark/type :text
                                   :mark/text "Hello"
                                   :mark/pos [0 0]
                                   :mark/font-size 50
                                   :mark/font-name "GothaPro"}
                                    
                                  {:mark/type :text
                                   :mark/text "Friend"
                                   :mark/pos [50 60]
                                   :mark/font-size 50
                                   :mark/font-name "GothaPro"}

                                  {:mark/type :svg
                                   :mark/url "assets/svg/paranoid.svg"
                                   :mark/pos [80 80]}
                                    
                                  {:mark/text "What's up?"
                                   :mark/type :text
                                   :mark/pos [30 50]
                                   :mark/font-size 50
                                   :mark/font-name "GothaPro"}]}])
