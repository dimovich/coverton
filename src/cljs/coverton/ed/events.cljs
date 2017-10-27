(ns coverton.ed.events
  (:require [re-frame.core  :as rf :refer [reg-event-db path trim-v dispatch dispatch-sync reg-event-fx]]
            [coverton.ed.db :refer [default-db]]
            [taoensso.timbre :refer-macros [info]]
            [coverton.ajax.events :as ajax-evt]
            [dommy.core     :as d :refer [sel1]]
            [coverton.util  :as util :refer [merge-db form-data]]))



(def ed-interceptors      [(path [:ed])               trim-v])
(def cover-interceptors   [(path [:ed :cover])        trim-v])



(reg-event-db
 ::initialize
 ed-interceptors
 (fn [db [cover]]
   (info "initializing with " cover)
   {:cover (or cover default-db)
    :t (inc (:t db))}))


(reg-event-db
 ::update
 ed-interceptors
 (fn [db [k f & args]]
   (apply update db k f args)))


(reg-event-db
 ::merge
 ed-interceptors
 merge-db)


(reg-event-db
 ::update-cover
 cover-interceptors
 (fn [db [k f & args]]
   (apply update db k f args)))


(reg-event-db
 ::merge-cover
 cover-interceptors
 merge-db)



(reg-event-fx
 ::upload-cover
 cover-interceptors
 (fn [{db :db} [& props]]
   (let [cover (apply merge db props)]
     (info "uploading cover...")
     {:dispatch
      [::ajax-evt/request-auth {:method :post
                                :uri "/save-cover"
                                :params cover
                                :on-success [::merge-cover]}]})))




(reg-event-fx
 ::get-cover
 (fn [_ [_ id]]
   {:dispatch
    [::ajax-evt/request-auth {:method :post
                              :uri "/get-cover"
                              :params {:id id}
                              :on-success [::initialize]}]}))



(reg-event-fx
 ::upload-file
 (fn [_ [_ data & [args]]]
   (when data
     (info "uploading file...")
     {:dispatch
      [::ajax-evt/request-auth (-> {:method :post
                                    :uri "/upload-file"
                                    :body data}
                                   (merge args))]})))





(defn set-active-mark [id]
  (dispatch [::merge-cover {:active-mark id}]))


(defn set-background [url]
  (dispatch [::merge-cover {:cover/background url}]))


(defn initialize [& [cover]]
  (dispatch-sync [::initialize cover]))



