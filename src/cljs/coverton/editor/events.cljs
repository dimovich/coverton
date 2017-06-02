(ns coverton.editor.events
  (:require [re-frame.core :as rf :refer [reg-event-db path trim-v dispatch]]
            [coverton.editor.db :refer [default-value]]
            [coverton.fonts :refer [default-font]]))



(def items-interceptors [(path :items) trim-v])
(def dim-interceptors   [(path :dim) trim-v])


(reg-event-db
 :initialize
 (fn [_ _]
   default-value))


(reg-event-db
 :toggle-dim
 dim-interceptors
 (fn [dim _]
   (not dim)))


(reg-event-db
 :update-font-size
 items-interceptors
 (fn [items [id size]]
   (assoc-in items [id :font :font-size] size)))


(reg-event-db
 :update-dom
 items-interceptors
 (fn [items [id dom]]
   (assoc-in items [id :dom] dom)))


(reg-event-db
 :add-item
 items-interceptors
 (fn [items [v]]
   (assoc items (str (random-uuid)) v)))


(rf/reg-event-db
 :remove-item
 items-interceptors
 (fn [items [id]]
   (dissoc items id)))


(reg-event-db
 :update-item
 items-interceptors
 (fn [items [id ks v]]
   (assoc-in items (into [id] ks) v)))



(defn handle-add-item [e]
  (let [rect (.. e -target -parentNode getBoundingClientRect)
        px (.. rect -left)
        py (.. rect -top)]
    (dispatch [:add-item {:x (- (- (.. e -clientX) px) 5)
                          :y (- (- (.. e -clientY) py) 5)
                          :font default-font}])))



(defn handle-remove-item [e]
  (let [text (.. e -target -value)
        id   (.. e -target -id)]
    (when (empty? text)
      (dispatch [:remove-item id]))))

