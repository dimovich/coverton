(ns coverton.ed.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]))


(reg-sub
 ::ed
 (fn [db _]
   (:ed db)))


(reg-sub
 ::items
 :<- [::ed]
 (fn [db _]
   (:items db))) ;;fixme ::items

;; continuous saving...
(reg-sub
 ::cover
 :<- [::items]
 (fn [items _]
   {:image-url "assets/img/coverton.jpg"
    :size [400 400]
    :tags ["hello"]
    :marks (vals items)}))

(reg-sub
 ::item-ids
 :<- [::items]
 (fn [items _]
   (keys items)))


(reg-sub
 ::item
 :<- [::items]
 (fn [items [_ id]]
   (get items id)))


(reg-sub
 ::dim
 :<- [::ed]
 (fn [db _]
   (:dim db)))


(reg-sub
 ::item-font-family
 :<- [::items]
 (fn [items [_ id]]
   (get-in items [id :font-family])))


(reg-sub
 ::item-font-size
 :<- [::items]
 (fn [items [_ id]]
   (get-in items [id :font-size])))


(reg-sub
 ::item-text
 :<- [::items]
 (fn [items [_ id]]
   (get-in items [id :text])))


(reg-sub
 ::item-pos
 :<- [::items]
 (fn [items [_ id]]
   (get-in items [id :pos])))


#_(reg-sub
   :a-b-sub
   (fn [q-vec d-vec]
     [(subs/subscribe [:a-sub])
      (subs/subscribe [:b-sub])])
   (fn [[a b] [_]] {:a a :b b}))
