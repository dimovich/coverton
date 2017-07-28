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


(defn init []
  (let [schema (concat cover-schema) ;;mark-schema
        conn (connect)
        _    (println conn)]
    (swap! db-state assoc :conn conn)
    (<!! (client/transact conn {:tx-data schema}))

    (info "db initialized")
    
    conn))


(defn current-db []
  (println "db-state " @db-state)
  (client/db (:conn @db-state)))


(defn add-data [data]
  (let [data (if (vector? data) data [data])
        conn (:conn @db-state)
        _ (println "add-date:   " conn)]
    (->> {:tx-data data}
         (client/transact (:conn @db-state))
         <!!)))



(defn get-all-covers []
  (let [db (current-db)]
    (->> {:query '[:find (pull ?e [*])
                   :where
                   [?e :cover/id]]
          :args [db]}
         (client/q (:conn @db-state))
         <!!
         (map first))))



(defn get-cover-by-eid [eid]
  (let [db (current-db)]
    (->> {:selector '[*]
          :eid eid}
         (client/pull db)
         <!!)))


(defn get-cover [id]
  (let [db (current-db)]
    (->> {:query '[:find (pull ?e [*])
                   :in $ ?id
                   :where
                   [?e :cover/id ?id]]
          :args [db id]}
         (client/q (:conn @db-state))
         <!!
         ffirst)))













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
