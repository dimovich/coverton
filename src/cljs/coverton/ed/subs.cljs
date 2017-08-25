(ns coverton.ed.subs
  (:require [re-frame.core      :refer [reg-sub subscribe]]
            [coverton.db.schema :refer [mark->db-map cover->db-map]]
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
 ::dimmer
 :<- [::ed]
 (fn [db _]
   (:dimmer db)))


(reg-sub
 ::cover
 :<- [::ed]
 (fn [db _]
   (:cover db)))


(reg-sub
 ::marks
 :<- [::cover]
 (fn [db _]
   (:marks db)))


(reg-sub
 ::size
 :<- [::cover]
 (fn [db _]
   (:size db)))


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
 ::mark-font-family
 :<- [::marks]
 (fn [marks [_ id]]
   (get-in marks [id :font-family])))


(reg-sub
 ::mark-font-size
 :<- [::marks]
 :<- [::size]
 (fn [[marks size] [_ id]]
   (let [[w h] size
         fs    (get-in marks [id :font-size])]
     (* fs h))))


(reg-sub
 ::mark-text
 :<- [::marks]
 (fn [marks [_ id]]
   (get-in marks [id :text])))


(reg-sub
 ::mark-pos
 :<- [::marks]
 :<- [::size]
 (fn [[marks size] [_ id]]
   ;; convert relative to absolute xy
   (let [[x y] (get-in marks [id :pos])
         [w h] size]
     [(* x w)
      (* y h)])))


(reg-sub
 ::image-url
 :<- [::cover]
 (fn [db _]
   (:image-url db)))


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
 (fn [[cover id] _]
   (if id
     (get-in cover [:marks id :color])
     (get-in cover [:font :color]))))


(reg-sub
 ::ref
 :<- [::marks]
 (fn [marks [_ id]]
   (get-in marks [id :ref])))




(defn export-cover []
  ;; extract saveable fields
  (-> @(subscribe [::cover])
      (select-keys (keys cover->db-map))
      (update-in [:marks]
                 #(reduce (fn [m [k v]]
                            (assoc m k (select-keys v (keys mark->db-map))))
                          {}
                          %))))




#_(reg-sub
   :a-b-sub
   (fn [q-vec d-vec]
     [(subs/subscribe [:a-sub])
      (subs/subscribe [:b-sub])])
   (fn [[a b] [_]] {:a a :b b}))
