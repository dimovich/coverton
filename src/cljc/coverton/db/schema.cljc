(ns coverton.db.schema)


(def magic-id #uuid "1d822372-983a-4012-adbc-569f412733fd")


(def cover->db-map
  {:image-url :cover/image-url
   :tags      :cover/tags
   :author    :cover/author
   :parent    :cover/parent
   :marks     :cover/marks
   :cover-id  :cover/id})


(def mark->db-map
  {:mark-id     :mark/id
   :pos         :mark/pos
   :font-size   :mark/font-size
   :font-family :mark/font-family
   :text        :mark/text
   :color       :mark/color
   :static      :mark/static})



(def user-schema [{:db/ident       :user/username
                   :db/valueType   :db.type/string
                   :db/cardinality :db.cardinality/one
                   :db/unique      :db.unique/identity}

                  {:db/ident       :user/password
                   :db/valueType   :db.type/string
                   :db/cardinality :db.cardinality/one}

                  {:db/ident       :user/email
                   :db/valueType   :db.type/string
                   :db/cardinality :db.cardinality/one}])



(def cover-schema [{:db/ident       :cover/id
                    :db/valueType   :db.type/uuid
                    :db/cardinality :db.cardinality/one
                    :db/unique      :db.unique/identity
                    :db/doc "Cover id"}

                   {:db/ident       :cover/data
                    :db/valueType   :db.type/bytes
                    :db/cardinality :db.cardinality/one
                    :db/doc "Cover fressian data"}

                   {:db/ident :cover/author
                    :db/valueType :db.type/ref
                    :db/cardinality :db.cardinality/one
                    :db/doc "Cover author"}

                   {:db/ident :cover/image-url
                    :db/valueType :db.type/string
                    :db/cardinality :db.cardinality/one
                    :db/doc "Cover image url"}

                   {:db/ident :cover/tags
                    :db/valueType :db.type/string
                    :db/cardinality :db.cardinality/many
                    :db/fulltext true
                    :db/doc "Cover tags"}


                   #_(
                      
                      {:db/ident :cover/marks
                       :db/valueType :db.type/ref
                       :db/cardinality :db.cardinality/many
                       :db/isComponent true
                       :db/doc "Cover marks (labels, svgs, etc)"})])



(def mark-schema [{:db/ident :mark/id
                   :db/valueType :db.type/uuid
                   :db/cardinality :db.cardinality/one
                   :db/unique :db.unique/identity}
                  
                  {:db/ident :mark/font-family
                   :db/valueType :db.type/string
                   :db/cardinality :db.cardinality/one}
                  
                  {:db/ident :mark/font-size
                   :db/valueType :db.type/long
                   :db/cardinality :db.cardinality/one}

                  {:db/ident :mark/color
                   :db/valueType :db.type/string
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



(def sample-data
  [{:cover/image-url "assets/img/coverton.jpg"
    :cover/tags ["sea" "boat" "depth" "children"]
    :cover/size [400 400]
    :cover/marks  [{:mark/type :text
                    :mark/text "Hello"
                    :mark/pos [0 0]
                    :mark/font-size 50
                    :mark/font-family "GothaPro"}
                                    
                   {:mark/type :text
                    :mark/text "Friend"
                    :mark/pos [50 60]
                    :mark/font-size 50
                    :mark/font-family "GothaPro"}

                   {:mark/type :svg
                    :mark/url "assets/svg/paranoid.svg"
                    :mark/pos [80 80]}
                                    
                   {:mark/text "What's up?"
                    :mark/type :text
                    :mark/pos [30 50]
                    :mark/font-size 50
                    :mark/font-family "GothaPro"}]}])
