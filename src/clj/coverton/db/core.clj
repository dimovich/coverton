(ns coverton.db.core
  (:require [clojure.core.async :refer [<!!]]
            [datomic.client     :as    client]
            [taoensso.timbre    :refer [info]]
            [coverton.db.schema :refer [coverton-schema]]
            [coverton.util      :refer [random-uuid]]))


(defonce db-state (atom {}))


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


(defn current-db []
  (client/db (get-connection)))



(defn transact [data]
  (let [data (if (sequential? data) data [data])]
    (info (-> (get-connection)
              (client/transact {:tx-data data})
              <!!))))



(defn query-db [q & args]
  (let [db (current-db)
        conn (get-connection)]
    (->> {:query q
          :args (into [db] args)}
         (client/q conn)
         <!!)))



(defn retract-entity [id]
  (transact [[:db.fn/retractEntity id]]))


(defn retract-attr [id attr-id attr]
  (transact [[:db/retract id attr-id attr]]))




(defn init []
  ;; add schema
  (-> coverton-schema
      transact)
  
  (info "db initialized"))









#_(map #(select-keys (first %)
                     [:cover/image-url
                      :cover/tags
                      :cover/marks])
       data)

#_(<!! (client/pull db {:selector '[*]
                        :eid 17592186045425}))



;; bin/run -m datomic.peer-server -h localhost -p 8998 -a admin,admin -d hello,datomic:mem://hello


;;(d/transact conn [{:db/id order-id :order/lineItems [{:lineItem/product chocolate :lineItem/quantity 1} {:lineItem/product whisky :lineItem/quantity 2}]}]



;; pull entity map as db -> d/entity-db... vs. d/entity
