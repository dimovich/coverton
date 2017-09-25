(ns coverton.db.users
  (:require [coverton.db.core :as db]))



(defn add-user [{:keys [username password email]}]
  (-> [{:user/username username
        :user/password password
        :user/email email}]
      db/transact))


(defn get-user [username]
  (-> '[:find  (pull ?e [*])
        :in    $ ?name
        :where [?e :user/username ?name]]
      (db/query-db username)
      ffirst))


(defn get-all-users []
  (->> '[:find  (pull ?e [*])
         :where [?e :user/username]]
       db/query-db
       (map #(-> %
                 first
                 (dissoc :db/id)))))
