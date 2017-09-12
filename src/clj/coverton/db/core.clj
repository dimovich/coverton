(ns coverton.db.core
  (:require [clojure.core.async :refer [<!!]]
            [datomic.client     :as    client]
            [clojure.pprint     :refer [pprint]]
            [buddy.hashers      :as hashers]
            [taoensso.timbre    :refer [info]]
            [coverton.db.schema :refer [cover-schema mark-schema user-schema magic-id]]))


(def db-state (atom {}))

(def users [{:username "dimovich"
             :password (hashers/derive "secret")
             :email   "some@random.com"}])

(defn random-uuid []
  (java.util.UUID/randomUUID))


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



(defn add-data [data]
  (let [data (if (sequential? data) data [data])]
    (info (-> (get-connection)
              (client/transact {:tx-data data})
              <!!))))



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



(defn add-user [{:keys [username password email]}]
  (-> [{:user/username username
        :user/password password
        :user/email email}]
      add-data))



(defn get-user [username]
  (let [db (current-db)
        conn (get-connection)]
    (->> {:query '[:find (pull ?e [*])
                   :in $ ?name
                   :where
                   [?e :user/username ?name]]
          :args [db username]}
         (client/q conn)
         <!!
         ffirst)))



(defn init []
  ;;schema
  (-> (concat cover-schema user-schema)
      add-data)

  ;;users
  (doall (map add-user users))
  
  (info "db initialized")
  (get-connection))





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


;;(d/transact conn [{:db/id order-id :order/lineItems [{:lineItem/product chocolate :lineItem/quantity 1} {:lineItem/product whisky :lineItem/quantity 2}]}]
