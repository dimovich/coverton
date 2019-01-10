(ns coverton.db.util
  (:require [coverton.db.core :as db]
            [coverton.db.covers :as covers]
            [coverton.db.users :as users]
            [coverton.db.invite :as invite]
            [clojure.data.fressian :as fress]
            [roll.util :refer [when-read]]))



(defn export-db-file [col fname]
  (->> (into [] col)
       clojure.pprint/pprint
       with-out-str
       (spit fname)))



(defn export-db []
  (->> ["db/invites.edn" invite/get-all-invites
        "db/covers.edn"  covers/get-all-covers
        "db/users.edn"   users/get-all-users]

       (partition 2)

       (map (fn [[p f]] (export-db-file (f) p)))))



(defn import-db-file [fname & [{f :fn :or {f identity}}]]
  (when-read [data fname]
    (some->> data
             read-string
             (map f)
             db/transact)))



(defn import-db []
  (import-db-file "db/invites.edn")
  (import-db-file "db/users.edn")
  (import-db-file "db/covers.edn"
                  {:fn (fn [m]
                         (update m :cover/data
                                 #(.array (fress/write %))))}))



