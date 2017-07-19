(ns coverton.ed.events
  (:require [re-frame.core :as rf :refer [reg-event-db path trim-v dispatch]]
            [coverton.ed.db :refer [default-value]]
            [coverton.fonts :refer [default-font]]
            [dommy.core :as d :refer [sel1]]))



(def ed-interceptors    [(path [:ed])        trim-v])
(def marks-interceptors [(path [:ed :marks]) trim-v])
(def dim-interceptors   [(path [:ed :dim])   trim-v])



(reg-event-db
 ::initialize
 ed-interceptors
 (fn [db _] ;; second parameter could be the image-url
   (merge default-value db)))


(reg-event-db
 ::update
 ed-interceptors
 (fn [db [ks v]]
   (assoc-in db ks v)))


(reg-event-db
 ::dim
 dim-interceptors
 (fn [dim _]
   (not dim)))


(reg-event-db
 ::update-font-size
 marks-interceptors
 (fn [marks [id size]]
   (assoc-in marks [id :font-size] size)))


(reg-event-db
 ::add-mark
 marks-interceptors
 (fn [marks [m]]
   (let [id (or (:mark-id m) (random-uuid))
         m  (assoc m :mark-id id)]
     (assoc marks (str id) m))))


(reg-event-db
 ::remove-mark
 marks-interceptors
 (fn [marks [id]]
   (dissoc marks id)))


(reg-event-db
 ::update-mark
 marks-interceptors
 (fn [marks [id ks v]]
   (if (and (get marks id)
            (not= (get-in marks (into [id] ks))
                  v))
     (assoc-in marks (into [id] ks) v)
     marks)))


(defn handle-add-mark [e]
  (let [x (.. e -clientX)
        y (.. e -clientY)
        rect (.. e -target -parentNode getBoundingClientRect)
        px (.. rect -left)
        py (.. rect -top)]
    (dispatch [::add-mark (merge {:pos [(- x px) (- y py)]}
                                 default-font)])))



(defn handle-remove-mark [e]
  (let [text (.. e -target -value)
        id   (.. e -target -id)]
    (when (empty? text)
      (dispatch [::remove-mark id]))))



(defn update-cover-id [id]
  (dispatch [::update [:cover-id] id]))



(defn update-size [size]
  (dispatch [::update [:size] size]))



(defn update-image-url [url]
  (dispatch [::update [:image-url] url]))



(defn update-pos [id pos]
  (dispatch [::update-mark id [:pos] pos]))

