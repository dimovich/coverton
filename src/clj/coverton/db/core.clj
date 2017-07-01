(ns coverton.db.core
  (:require [clojure.core.async :refer [<!!]]
            [datomic.client     :as    client]
            [clojure.pprint     :refer [pprint]]
            [taoensso.timbre    :refer [info]]
            [coverton.db.schema :refer [cover-schema mark-schema sample-data]]))


(def db-state (atom {:conn nil}))


(defn connect []
  (->> {:db-name "hello"
        :account-id client/PRO_ACCOUNT
        :secret "admin"
        :region "none"
        :endpoint "localhost:8998"
        :service "peer-server"
        :access-key "admin"}
                  
       client/connect
       <!!))

(def conn (memoize connect))


(defn init []
  (let [schema (concat cover-schema ;;mark-schema
                       )
        conn (conn)]
    
    (<!! (client/transact conn {:tx-data schema}))

    (info "db initialized")
    
    conn))



(defn add-data [data]
  (let [data (if (vector? data) data [data])]
    (->> {:tx-data data}
         (client/transact (conn))
         <!!)))



(defn get-all-covers []
  (let [conn (conn)
        db (client/db conn)]
    (->> {:query '[:find (pull ?e [*])
                   :where
                   [?e :cover/id]]
          :args [db]}
         (client/q conn)
         <!!
         (map first))))



(defn get-cover [eid]
  (let [db (client/db (conn))]
    (->> {:selector '[*]
          :eid eid}
         (client/pull db)
         <!!)))











#_(map #(select-keys (first %)
                     [:cover/image-url
                      :cover/tags
                      :cover/marks])
       data)

#_(<!! (client/pull db {:selector '[*]
                        :eid 17592186045425}))



;; pass data to server
;;  - save button
;;


;; check if only one entity
;; pass back to client
;; continuous saving
