(ns coverton.db.core
  (:require [clojure.core.async :refer [<!!]]
            [datomic.client     :as    client]
            [coverton.db.schema :refer [cover-schema mark-schema sample-data]]))


(defn init [schema]
  (let [conn (<!! (client/connect
                   {:db-name "hello"
                    :account-id client/PRO_ACCOUNT
                    :secret "admin"
                    :region "none"
                    :endpoint "localhost:8998"
                    :service "peer-server"
                    :access-key "admin"}))]
    
    (<!! (client/transact conn {:tx-data schema}))
    
    conn))


(defn add-data [conn data]
  (<!! (client/transact conn {:tx-data data})))


(defn get-all-covers [conn]
  (let [db (client/db conn)]
    (->> {:query '[:find (pull ?e [*])
                   :where
                   [?e :cover/image-url]]
          :args [db]}
         (client/q conn)
         <!!
         (map first))))















#_(map #(select-keys (first %)
                     [:cover/image-url
                      :cover/tags
                      :cover/marks])
       data)

#_(<!! (client/pull db {:selector '[*]
                        :eid #{17592186045425 17592186045422}}))



;; pass data to server
;;  - save button
;;
