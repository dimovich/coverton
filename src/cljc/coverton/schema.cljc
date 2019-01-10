(ns coverton.schema)


(def cover->db [:cover/id :cover/tags :cover/author :cover/data])


(def schema
  { ;; user
   :user/email {:db/cardinality :db.cardinality/one
                :db/unique      :db.unique/identity}
   
   :user/password {:db/cardinality :db.cardinality/one}


   ;; invite
   :invite/secret {:db/cardinality :db.cardinality/one}
   
   ;; :new / :confirmed / :sent / :expired
   :invite/status {:db/cardinality :db.cardinality/one}

   :invite/email {:db/cardinality :db.cardinality/one
                  :db/unique :db.unique/identity}

   :invite/story {:db/cardinality :db.cardinality/one}

   
   ;; cover
   :cover/id {:db/cardinality :db.cardinality/one
              :db/unique :db.unique/identity}

   :cover/data   {:db/cardinality :db.cardinality/one}
   
   :cover/author {:db/valueType :db.type/ref
                  :db/cardinality :db.cardinality/one}
   
   :cover/image-url {:db/cardinality :db.cardinality/one}
   
   :cover/tags {:db/cardinality :db.cardinality/many}})



;;(defonce magic-id #uuid "1d822372-983a-4012-adbc-569f412733fd")


