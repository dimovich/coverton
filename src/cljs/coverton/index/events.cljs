(ns coverton.index.events
  (:require [re-frame.core :as rf :refer [reg-event-db path trim-v dispatch]]
            [coverton.index.db :refer [default-value]]
            [dommy.core :as d]))



(def index-interceptors        [(path :index)                   trim-v])
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
 ::push-panel
 panel-interceptors
 (fn [panels [p]]
   (list* p panels)))


(reg-event-db
 ::pop-panel
 panel-interceptors
 (fn [panels _]
   (rest panels)))


(defn push-panel [p]
  (dispatch [::push-panel p]))


(defn pop-panel []
  (dispatch [::pop-panel]))
