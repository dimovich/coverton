(ns coverton.db.core
  (:require [clojure.core.async :refer [<!!]]
            [datomic.client     :as    client]
            [clojure.pprint     :refer [pprint]]
            [buddy.hashers      :as hashers]
            [taoensso.timbre    :refer [info]]
            [coverton.db.schema :refer [cover-schema mark-schema user-schema magic-id]]
            [clojure.data.fressian :as fress]
            [coverton.util      :refer [random-uuid]]))


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
         (map #(-> %
                   first
                   (update :cover/data fress/read)
                   (dissoc :db/id))))))



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


(defn get-all-users []
  (let [db (current-db)
        conn (get-connection)]
    
    (->> {:query '[:find  (pull ?e [*])
                   :where [?e :user/username]]
          :args [db]}
         
         (client/q conn)
         <!!
         (map #(-> %
                   first
                   (dissoc :db/id))))))




(defn export-db-file [col fname]
  (->> (into [] col)
       clojure.pprint/pprint
       with-out-str
       (spit fname)))


(defn export-db []
  (export-db-file (get-all-covers) "db/covers.edn")
  (export-db-file (get-all-users)  "db/users.edn"))



(defn import-db-file [fname & [{f :fn :or {f identity}}]]
  (some->> (slurp fname)
           read-string
           (map f)
           add-data))

(defn import-db []
  (import-db-file "db/users.edn")
  (import-db-file "db/covers.edn"
                  {:fn (fn [m]
                         (update m :cover/data
                                 #(.array (fress/write %))))}))






(defn init []
  ;; add schema
  (-> (concat cover-schema user-schema)
      add-data)

  ;; import covers and users
  (import-db)
  
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
