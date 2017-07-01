(ns coverton.ed.subs
  (:require [re-frame.core  :refer [reg-sub subscribe]]))


(reg-sub
 ::ed
 (fn [db _]
   (:ed db)))


(reg-sub
 ::marks
 :<- [::ed]
 (fn [db _]
   (:marks db))) ;;fixme ::items


;; continuous saving...
(reg-sub
 ::cover
 :<- [::ed]
 (fn [db _]
   (-> db
       (select-keys [:cover-id :image-url :size :tags :marks])
       (update-in [:marks]
                  #(map (fn [[k m]]
                          (select-keys m [:font-size :font-family
                                          :mark-id :pos :text :color]))
                        %)))))

(reg-sub
 ::mark-ids
 :<- [::marks]
 (fn [marks _]
   (keys marks)))


(reg-sub
 ::mark
 :<- [::marks]
 (fn [marks [_ id]]
   (get marks id)))


(reg-sub
 ::dim
 :<- [::ed]
 (fn [db _]
   (:dim db)))


(reg-sub
 ::mark-font-family
 :<- [::marks]
 (fn [marks [_ id]]
   (get-in marks [id :font-family])))


(reg-sub
 ::mark-font-size
 :<- [::marks]
 (fn [marks [_ id]]
   (get-in marks [id :font-size])))


(reg-sub
 ::mark-text
 :<- [::marks]
 (fn [marks [_ id]]
   (get-in marks [id :text])))


(reg-sub
 ::mark-pos
 :<- [::marks]
 (fn [marks [_ id]]
   (get-in marks [id :pos])))


#_(reg-sub
   :a-b-sub
   (fn [q-vec d-vec]
     [(subs/subscribe [:a-sub])
      (subs/subscribe [:b-sub])])
   (fn [[a b] [_]] {:a a :b b}))



;; load cover (eid)

