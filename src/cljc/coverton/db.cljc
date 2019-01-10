(ns coverton.db
  (:require [datascript.core :as ds]
            [linked.core :as linked]
            [taoensso.timbre :refer [info]]
            [coverton.schema :refer [schema]]
            [re-frame.core :as rf :refer [reg-event-db reg-sub
                                          dispatch-sync dispatch
                                          subscribe path trim-v
                                          reg-event-fx reg-fx]]))




(def default-opts {})


(def db-interceptors
  [(path :db) trim-v])



(reg-event-db
 ::init
 db-interceptors
 (fn [old-db-opts [db-opts]]
   (merge default-opts old-db-opts db-opts)))



(reg-fx
 ::run-fn
 (fn [f] (when f (f))))



(reg-sub
 ::db
 (fn [db _]
   (:db db)))


(reg-event-db
 ::tick
 db-interceptors
 (fn [db _]
  (update db :tick inc)))



(reg-sub
 ::tick
 :<- [::db]
 (fn [{tick :tick} _]
   tick))



(reg-sub
 ::db-conn
 :<- [::db]
 (fn [db _] (:db-conn db)))



(reg-sub
 ::db-value
 :<- [::db-conn]
 :<- [::tick]
 (fn [[conn _] _]
   (some-> conn deref)))



(reg-sub
 ::db-loaded?
 :<- [::db]
 (fn [db _]
   (:db-loaded? db)))



(reg-sub
 ::search-tags
 :<- [::db]
 (fn [db _]
   (:search-tags db)))



(reg-sub
 ::default-db
 :<- [::db-value]
 (fn [db [_ sort-index]]
   (some->
    db (ds/pull-many
        '[*] (->> sort-index
                  (ds/datoms db :avet)
                  (mapv :e))))))


(reg-sub
 ::default-db-syms
 (fn [_ [_ index]]
   (->> @(subscribe [::default-db index])
        (mapv :symbol)
        (into (linked/set)))))




(reg-sub
 ::descryptor
 :<- [::db-value]
 (fn [db [_ sym]]
   (when db
     (some->> [:symbol sym]
              (ds/entity db)
              :db/id
              (ds/pull db '[*])))))



(reg-fx
 ::transact
 (fn [args]
   (apply ds/transact! args)))



(reg-event-fx
 ::load
 db-interceptors
 (fn [{:keys [db]} [data]]
  (when-let [conn (:db-conn db)]
    {::transact [conn data]
     :dispatch [::tick]})))



(reg-event-fx
 ::load-db
 db-interceptors
 (fn [{:keys [db]} [new-db-conn]]
  
  (cond-> {:db (-> db (assoc :db-loaded? true
                             :db-conn new-db-conn))
           :dispatch [::tick]}
    ;; run triggers
    (:on-db-load db) (assoc ::run-fn (:on-db-load db)))))



(defn load-data [data]
  (dispatch [::load data]))


(defn load-db [new-db]
  (dispatch [::load-db (ds/conn-from-db new-db)]))


(defn init [& [{:keys [db-conn] :as db}]]
  (let [conn (or db-conn (ds/create-conn schema))]
    (dispatch-sync
     [::init (-> {:db-conn conn
                  :tick 0}
                 (merge db))])))
