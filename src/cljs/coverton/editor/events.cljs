(ns coverton.editor.events
  (:require [re-frame.core :as rf :refer [reg-event-db path trim-v dispatch]]
            [coverton.editor.db :refer [default-value]]
            [coverton.fonts :refer [default-font]]))


(defn allocate-next-id
  [items]
  ((fnil inc 0) (last (keys items))))


(def items-interceptors [(path :items) trim-v])


(reg-event-db
 :initialize
 (fn [_ _]
   default-value))



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

