(ns coverton.db.core
  (:require [clojure.core.async :refer [<!!]]
            [datomic.client     :as    client]
            [clojure.pprint     :refer [pprint]]
            [taoensso.timbre    :refer [info]]
            [coverton.db.schema :refer [cover-schema mark-schema sample-data]]))


(def db-state (atom {}))


(defn connect []
  (-> {:db-name "hello"
       :account-id client/PRO_ACCOUNT
       :secret "admin"
       :region "none"
       :endpoint "localhost:8998"
       :service "peer-server"
       :access-key "admin"}
      
      client/connect
      <!!))


(defn get-connection []
  (if-let [conn (:conn @db-state)]
    conn
    (let [conn (connect)]
      (swap! db-state assoc :conn conn)
      conn)))


(defn init []
  (let [schema (concat cover-schema) ;;mark-schema
        conn (get-connection)]
    (swap! db-state assoc :conn conn)
    (<!! (client/transact conn {:tx-data schema}))

    (info "db initialized")
    
    conn))


(defn current-db []
  (client/db (get-connection)))


(defn add-data [data]
  (let [data (if (vector? data) data [data])
        conn (get-connection)]
    (->> {:tx-data data}
         (client/transact conn)
         <!!)))



(defn get-all-covers []
  (let [db (current-db)
        conn (get-connection)]
    (->> {:query '[:find (pull ?e [*])
                   :where
                   [?e :cover/id]]
          :args [db]}
         (client/q conn)
         <!!
         (map first))))



(defn get-cover-by-eid [eid]
  (let [db (current-db)]
    (->> {:selector '[*]
          :eid eid}
         (client/pull db)
         <!!)))


(defn get-cover [id]
  (let [db (current-db)
        conn (get-connection)]
    (->> {:query '[:find (pull ?e [*])
                   :in $ ?id
                   :where
                   [?e :cover/id ?id]]
          :args [db id]}
         (client/q conn)
         <!!
         ffirst)))


(defn export-covers-to-file [fname])









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

;; bin/run -m datomic.peer-server -h localhost -p 8998 -a admin,admin -d hello,datomic:mem://hello
