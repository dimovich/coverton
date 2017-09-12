(ns coverton.index.events
  (:require [re-frame.core :as rf :refer [reg-event-db path trim-v reg-event-fx dispatch]]
            [coverton.index.db :refer [default-value]]
            [taoensso.timbre :refer-macros [info]]
            [coverton.ajax.events :as ajax-evt]))



(def index-interceptors        [(path :index)                  trim-v])
(def panel-interceptors        [(path [:index :panel-stack])   trim-v])



(reg-event-db
 ::initialize
 index-interceptors
 (fn [_ _]
   default-value))


(reg-event-db
 ::update
 index-interceptors
 (fn [db [ks v]]
   (assoc-in db ks v)))


(reg-event-db
 ::set-page
 index-interceptors
 (fn [db [k]]
   (assoc db :page k)))


(defn set-page [k]
  (dispatch [::set-page k]))


(reg-event-fx
 ::login
 index-interceptors
 (fn [_ [creds]]
   {:dispatch
    [::ajax-evt/request {:method     :post
                         :uri        "/login"
                         :params     creds
                         :on-success [::ajax-evt/set-token]}]}))



(reg-event-fx
 ::logout
 index-interceptors
 (fn [{db :db} _]
   {:db (dissoc db :authenticated?)
    :dispatch [::ajax-evt/remove-token]}))



(reg-event-fx
 ::get-covers
 index-interceptors
 (fn [_ [opts]]
   {:dispatch
    [::ajax-evt/request-auth {:method :post
                              :uri "/get-covers"
                              :params opts
                              :on-success [::import-covers]}]}))



(reg-event-db
 ::import-covers
 index-interceptors
 (fn [db [covers]]
   (assoc db :covers covers)))
