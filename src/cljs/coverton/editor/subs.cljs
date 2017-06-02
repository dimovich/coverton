(ns coverton.editor.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]))


(reg-sub
 :items
 (fn [db _]
   (:items db)))


(reg-sub
 :item
 :<- [:items]
 (fn [items [_ id]]
   (get items id)))


(reg-sub
 :items-with-dom
 :<- [:items]
 (fn [items _]
   (filter (fn [[k v]] (:dom v)) items)))


(reg-sub
 :font
 :<- [:items]
 (fn [items [_ id]]
   (get-in items [id :font])))


(reg-sub
 :dim
 (fn [db _]
   (:dim db)))



#_(reg-sub
   :a-b-sub
   (fn [q-vec d-vec]
     [(subs/subscribe [:a-sub])
      (subs/subscribe [:b-sub])])
   (fn [[a b] [_]] {:a a :b b}))
