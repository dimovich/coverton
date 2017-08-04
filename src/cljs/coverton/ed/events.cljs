(ns coverton.ed.events
  (:require [re-frame.core :as rf :refer [reg-event-db path trim-v dispatch dispatch-sync]]
            [coverton.ed.db :refer [default-value]]
            [coverton.fonts :refer [default-font]]
            [dommy.core :as d :refer [sel1]]))



(def ed-interceptors     [(path [:ed])         trim-v])
(def cover-interceptors  [(path [:ed :cover])  trim-v])
(def dimmer-interceptors [(path [:ed :dimmer]) trim-v])
(def marks-interceptors  [(path [:ed :cover :marks]) trim-v])



;; FINISH
(reg-event-db
 ::initialize
 ed-interceptors
 (fn [db [cover]]
   (let [t (inc (:t db))
         cover (or cover (:cover db))
         _ (println "initializing with " cover)]
     (merge default-value cover {:t t}))))


(reg-event-db
 ::update
 ed-interceptors
 (fn [db [ks v]]
   (assoc-in db ks v)))


(reg-event-db
 ::update-mark-pos
 ed-interceptors
 (fn [db [id pos]]
   (let [[w h] (:size db)
         [x y] pos]
     (assoc-in db [:marks id :pos] [(/ x w)
                                    (/ y h)]))))


(reg-event-db
 ::update-mark-font-size
 ed-interceptors
 (fn [db [id fsize]]
   (let [[w h] (:size db)]
     (assoc-in db [:marks id :font-size] (/ fsize h)))))



(reg-event-db
 ::dim
 dim-interceptors
 (fn [dim _]
   (not dim)))


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


(defn update-cover-id [id]
  (dispatch [::update [:cover-id] id]))


(defn update-size [size]
  (dispatch [::update [:size] size]))


(defn update-image-url [url]
  (dispatch-sync [::update [:image-url] url]))


(defn update-pos [id pos]
  (dispatch [::update-mark-pos id pos]))


(defn update-font-size [id fsize]
  (dispatch [::update-mark-font-size id fsize]))


(defn update-font-family [id family]
  (dispatch [::update-mark id [:font-family] family]))


(defn update-text [id text]
  (dispatch [::update-mark id [:text] text]))


(defn update-mark-static [id static]
  (dispatch [::update-mark id [:static] static]))


(defn update-dim [panel]
  (dispatch [::update [:dim] panel]))


(defn initialize [cover]
  (dispatch-sync [::initialize cover]))
