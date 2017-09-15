(ns coverton.index.events
  (:require [re-frame.core :as rf :refer [reg-event-db path trim-v reg-event-fx dispatch]]
            [coverton.index.db :refer [default-value]]
            [taoensso.timbre :refer-macros [info]]
            [coverton.ajax.events :as ajax-evt]
            [coverton.util :refer [merge-db]]))



(def index-interceptors [(path :index) trim-v])


(reg-event-fx
 ::initialize
 index-interceptors
 (fn [_ _]
   {:db default-value
    :dispatch [::get-covers]}))


(reg-event-db
 ::update
 index-interceptors
 merge-db)


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
 (fn [{db :db} [{:keys [username] :as creds}]]
   {:db (assoc db :user username )
    :dispatch
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
                              :on-success [::update]}]}))



