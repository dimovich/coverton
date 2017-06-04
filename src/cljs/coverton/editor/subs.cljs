(ns coverton.editor.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]))


(reg-sub
 :ed-items
 (fn [db _]
   (:items db)))


(reg-sub
 :ed-item-ids
 :<- [:ed-items]
 (fn [items _]
   (keys items)))


(reg-sub
 :ed-item
 :<- [:ed-items]
 (fn [items [_ id]]
   (get items id)))


(reg-sub
 :ed-dim
 (fn [db _]
   (:dim db)))


(reg-sub
 :ed-item-font-family
 :<- [:ed-items]
 (fn [items [_ id]]
   (get-in items [id :font :font-family])))


(reg-sub
 :ed-item-font-size
 :<- [:ed-items]
 (fn [items [_ id]]
   (get-in items [id :font :font-size])))


(reg-sub
 :ed-item-text
 :<- [:ed-items]
 (fn [items [_ id]]
   (get-in items [id :text])))


(reg-sub
 :ed-item-pos
 :<- [:ed-items]
 (fn [items [_ id]]
   (get-in items [id :pos])))


#_(reg-sub
   :a-b-sub
   (fn [q-vec d-vec]
     [(subs/subscribe [:a-sub])
      (subs/subscribe [:b-sub])])
   (fn [[a b] [_]] {:a a :b b}))
