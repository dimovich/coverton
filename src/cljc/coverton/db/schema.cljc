(ns coverton.db.schema)


;;todo: need timestamp for covers (datomic has builtin)

(defonce cover->db [:cover/id :cover/tags :cover/author :cover/data])


(defonce coverton-schema

  [ ;;
   ;; user
   ;;
   {:db/ident       :user/email
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/identity}
   
   {:db/ident       :user/password
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one}



   ;;
   ;; invite
   ;;

   {:db/ident :invite/secret
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one}
   
   ;; :new / :approved / :sent / :expired
   {:db/ident       :invite/status
    :db/valueType   :db.type/keyword
    :db/cardinality :db.cardinality/one}

   {:db/ident :invite/email
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique :db.unique/identity}

   {:db/ident :invite/story
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one}


   ;;
   ;; cover
   ;;
   {:db/ident       :cover/id
    :db/valueType   :db.type/uuid
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/identity
    :db/doc "Cover id"}

   {:db/ident       :cover/data
    :db/valueType   :db.type/bytes
    :db/cardinality :db.cardinality/one
    :db/doc "Cover fressian data"}

   {:db/ident :cover/author
    :db/valueType :db.type/string
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
    :db/doc "Cover tags"}])



;;(defonce magic-id #uuid "1d822372-983a-4012-adbc-569f412733fd")


