(ns coverton.ed.events
  (:require [re-frame.core :as rf :refer [reg-event-db path trim-v dispatch dispatch-sync]]
            [coverton.ed.db :refer [default-value]]
            [coverton.fonts :refer [default-font]]
            [coverton.util  :refer [info]]
            [dommy.core :as d :refer [sel1]]))



(def ed-interceptors     [(path [:ed])               trim-v])
(def dimmer-interceptors [(path [:ed :dimmer])       trim-v])
(def cover-interceptors  [(path [:ed :cover])        trim-v])
(def marks-interceptors  [(path [:ed :cover :marks]) trim-v])



(reg-event-db
 ::initialize
 ed-interceptors
 (fn [db [cover]]
   (let [cover (or cover (:cover db))
         _     (info "initializing with " cover)]
     {:cover (merge default-value cover)
      :t (inc (:t db))})))



(reg-event-db
 ::update
 ed-interceptors
 (fn [db [ks v]]
   (assoc-in db ks v)))



(reg-event-db
 ::update-cover
 cover-interceptors
 (fn [db [ks v]]
   (assoc-in db ks v)))



(reg-event-db
 ::update-mark-pos
 cover-interceptors
 (fn [db [id pos]]
   (let [[w h] (:size db)
         [x y] pos]
     (assoc-in db [:marks id :pos] [(/ x w)
                                    (/ y h)]))))


(reg-event-db
 ::update-mark-font-size
 cover-interceptors
 (fn [db [id fsize]]
   (let [[w h] (:size db)]
     (assoc-in db [:marks id :font-size] (/ fsize h)))))



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


(defn handle-add-mark [pos]
  (dispatch [::add-mark (merge {:pos pos}
                               default-font)]))


(defn handle-remove-mark [e]
  (let [text (.. e -target -value)
        id   (.. e -target -id)]
    (when (empty? text)
      (dispatch [::remove-mark id]))))


(defn set-cover-id [id]
  (dispatch [::update-cover [:cover-id] id]))


(defn set-size [size]
  (dispatch [::update-cover [:size] size]))


(defn set-image-url [url]
  (dispatch [::update-cover [:image-url] url]))


(defn set-pos [id pos]
  (dispatch [::update-mark-pos id pos]))


(defn set-font-size [id fsize]
  (dispatch [::update-mark-font-size id fsize]))


(defn set-font-family [id family]
  (dispatch [::update-mark id [:font-family] family]))


(defn set-color [id color]
  (dispatch [::update-mark id [:color] color]))


(defn set-text [id text]
  (dispatch [::update-mark id [:text] text]))


(defn set-mark-static [id static]
  (dispatch [::update-mark id [:static] static]))


(defn set-dimmer [panel]
  (dispatch [::update [:dimmer] panel]))


(defn set-active-mark [id]
  (dispatch [::update [:active-mark] id]))


(defn initialize [cover]
  (dispatch [::initialize cover]))
