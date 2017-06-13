(ns coverton.index.events
  (:require [re-frame.core :as rf :refer [reg-event-db path trim-v]]
            [coverton.index.db :refer [default-value]]
            [dommy.core :as d]))



(def index-interceptors        [(path :index)                   trim-v])
(def active-panel-interceptors [(path [:index :active-panel])   trim-v])



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
 ::set-active-panel
 active-panel-interceptors
 (fn [_ [panel]]
   panel))

