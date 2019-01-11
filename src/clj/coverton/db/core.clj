(ns coverton.db.core
  (:require [re-frame.core :refer [dispatch subscribe]]
            [datascript.core :as ds]
            [taoensso.timbre :refer [info]]
            [coverton.db :as db]))



(defn transact [data]
  (dispatch [::db/load-data (if (sequential? data) data [data])]))



(defn query-db [query & args]
  (apply ds/q query @(subscribe [::db/db-value]) args))



(defn retract-entity [id]
  (transact [[:db.fn/retractEntity id]]))



(defn retract-attr [id attr-id attr]
  (transact [[:db/retract id attr-id attr]]))














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
