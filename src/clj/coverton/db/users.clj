(ns coverton.db.users
  (:require [coverton.db.core :as db]))



(defn add-user [{:keys [email password]}]
  (-> [{:user/email email
        :user/password password}]
      db/transact))



(defn get-user-by-email [email]
  (-> '[:find  (pull ?e [*])
        :in    $ ?email
        :where [?e :user/email ?email]]
      (db/query-db email)
      ffirst))


(defn get-all-users []
  (->> '[:find  (pull ?e [*])
         :where [?e :user/email]]
       db/query-db
       (map #(-> %
                 first
                 (dissoc :db/id)))))





#_(defn get-user [username]
    (-> '[:find  (pull ?e [*])
          :in    $ ?name
          :where [?e :user/username ?name]]
        (db/query-db username)
        ffirst))
