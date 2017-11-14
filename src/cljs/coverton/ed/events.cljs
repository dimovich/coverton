(ns coverton.ed.events
  (:require [re-frame.core  :as rf :refer [reg-event-db path trim-v dispatch dispatch-sync reg-event-fx]]
            [coverton.ed.db :refer [default-db]]
            [taoensso.timbre :refer-macros [info]]
            [coverton.ajax.events :as ajax-evt]
            [dommy.core     :as d :refer [sel1]]
            [coverton.util  :as util :refer [merge-db form-data]]))



(def ed-interceptors    [(path [:ed])        trim-v])
(def cover-interceptors [(path [:ed :cover]) trim-v])



(reg-event-db
 ::initialize
 ed-interceptors
 (fn [db [cover]]
   (info "initializing with " cover)
   {:cover (or cover default-db)
    :tool :text}))


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
 (fn [{cover :db} [& [args]]]
   (info "uploading cover...")
   {:dispatch
    [::ajax-evt/request-auth (-> {:method :post
                                  :uri "/save-cover"
                                  :params cover
                                  :on-success [::merge-cover]}
                                 (merge args))]}))




(reg-event-fx
 ::upload-files
 cover-interceptors
 (fn [_ [files & [args]]]
   (info "uploading files..." files)
   (let [data (util/form-data files)]
     {:dispatch
      [::ajax-evt/request-auth (-> {:method :post
                                    :uri "/upload-file"
                                    :body data
                                    :on-success [::merge-cover]}
                                   (merge args))]})))





(defn set-active-mark [id]
  (dispatch [::merge-cover {:active-mark id}]))


(defn set-background [url]
  (dispatch [::merge-cover {:cover/background url}]))


(defn initialize [& [cover]]
  (dispatch-sync [::initialize cover]))


(defn add-files-to-upload [id file]
  (dispatch [::update :files-to-upload assoc id file]))
