(ns coverton.db.core
  (:require [clojure.core.async :refer [<!!]]
            [datomic.client     :as    client]
            [coverton.db.schema :refer [mark-types cover-schema mark-schema]]))

(def conn
    (<!! (client/connect
          {:db-name "hello"
           :account-id client/PRO_ACCOUNT
           :secret "admin"
           :region "none"
           :endpoint "localhost:8998"
           :service "peer-server"
           :access-key "admin"})))


(<!! (client/transact conn {:tx-data cover-schema}))

(<!! (client/transact conn {:tx-data mark-schema}))

(defn make-idents [xs]
  (mapv #(hash-map :db/ident %) xs))

(<!! (client/transact conn {:tx-data (make-idents mark-types)}))


(def coverton-data [{:cover/id (java.util.UUID/randomUUID)
                     :cover/image-url "assets/img/coverton.jpg"
                     :cover/tags ["sea" "boat" "depth" "children"]
                     :cover/marks  [{:mark/id "someid"
                                     :mark/type :text
                                     :mark/text "Hello"
                                     :mark/pos [0.55 0.7]
                                     :mark/font-size 50
                                     :mark/font-name "GothaPro"}
                                    
                                    {:mark/id "anotherid"
                                     :mark/type :text
                                     :mark/text "Friend"
                                     :mark/pos [0.55 0.7]
                                     :mark/font-size 50
                                     :mark/font-name "GothaPro"}

                                    {:mark/id "someotherid"
                                     :mark/text "What's up?"
                                     :mark/type :text
                                     :mark/pos [0.55 0.7]
                                     :mark/font-size 50
                                     :mark/font-name "FamilyPro"}]}])




(<!! (client/transact conn {:tx-data coverton-data}))


(def db (client/db conn))

(<!! (client/q conn {:args [db]
                     :query '[:find ?id
                              :where
                              [_ :cover/marks ?mark]
                              [?mark :mark/id ?id]]}))


(def data (<!! (client/q conn {:args [db]
                               :query '[:find
                                        (pull ?e [*])
                                        :where
                                        [?e :cover/id]]})))


(:cover/marks (ffirst data))

#_(<!! (client/pull db {:selector '[*]
                        :eid #{17592186045425 17592186045422}}))



