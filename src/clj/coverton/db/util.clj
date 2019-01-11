(ns coverton.db.util
  (:require [coverton.db.core :as db]
            [coverton.db.covers :as covers]
            [coverton.db.users :as users]
            [coverton.db.invite :as invite]
            [clojure.data.fressian :as fress]
            [taoensso.timbre :refer [info]]
            [roll.util :refer [when-read-edn write-edn]]))



(defn export-db []
  (->> ["db/invites.edn" invite/get-all-invites
        "db/covers.edn"  covers/get-all-covers
        "db/users.edn"   users/get-all-users]
       (partition 2)
       (map (fn [[path f]] (write-edn path (f))))))



(defn import-db-file [path & [{f :fn :or {f identity}}]]
  (when-read-edn [data path]
    (some->> data (map f) (db/transact))))



(defn import-db []
  (info "importing DB...")
  (import-db-file "db/invites.edn")
  (import-db-file "db/users.edn")
  (import-db-file "db/covers.edn"
                  {:fn (fn [m]
                         (-> m (update :cover/data
                                       #(.array (fress/write %)))
                             
                             ;; comment after export
                             (assoc :cover/sbls
                                    (->> m ((juxt :cover/author :cover/tags))
                                         (remove nil?)
                                         (flatten)
                                         (into #{})))))}))
