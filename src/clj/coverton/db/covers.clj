(ns coverton.db.covers
  (:require [coverton.db.core :as db]
            [clojure.data.fressian :as fress]))



(defn get-covers [{:keys [sbls]}]
  (->> sbls
       (db/query-db
        '[:find (pull ?e [*])
          :in $ [?sbl ...]
          :where [?e :cover/sbls ?sbl]])
       
       (map #(-> (first %)
                 (update :cover/data fress/read)
                 (dissoc :db/id)))))



(defn get-all-covers []
  (->> '[:find (pull ?e [*])
         :in $
         :where [?e :cover/id]]
       db/query-db
       (map #(-> (first %)
                 (update :cover/data fress/read)
                 (dissoc :db/id)))))



(defn get-cover [id]
  (-> '[:find (pull ?e [*])
        :in $ ?id
        :where [?e :cover/id ?id]]
      (db/query-db id)
      ffirst))







#_(defn get-cover-by-eid [eid]
    (let [db (current-db)]
      (->> {:selector '[*]
            :eid eid}
           (client/pull db)
           <!!)))
