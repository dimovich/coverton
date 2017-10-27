(ns coverton.ed.subs
  (:require [re-frame.core      :refer [reg-sub subscribe]]
            [coverton.fonts     :refer [default-font]]))


(reg-sub
 ::ed
 (fn [db _]
   (:ed db)))


;; time (datomic shnizzle)
(reg-sub
 ::t
 :<- [::ed]
 (fn [db _]
   (:t db)))


(reg-sub
 ::cover
 :<- [::ed]
 (fn [db _]
   (:cover db)))


(reg-sub
 ::keys
 :<- [::ed]
 (fn [db [_ ks]]
   (get-in db ks)))


(reg-sub
 ::background
 :<- [::cover]
 (fn [db _]
   (:cover/background db)))


(reg-sub
 ::color
 :<- [::marks]
 (fn [marks [_ id]]
   (get-in marks [id :color])))


(reg-sub
 ::active-mark
 :<- [::cover]
 (fn [db _]
   (:active-mark db)))


(reg-sub
 ::active-color
 :<- [::cover]
 :<- [::active-mark]
 (fn [[cover active-id] _]
   (or (get-in cover [:cover/marks active-id :color])
       (get-in cover [:cover/font :color]))))


