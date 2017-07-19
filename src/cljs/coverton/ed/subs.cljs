(ns coverton.ed.subs
  (:require [re-frame.core :refer [reg-sub]]
            [coverton.db.schema :refer [mark->db-map cover->db-map]]))


(reg-sub
 ::ed
 (fn [db _]
   (:ed db)))


(reg-sub
 ::marks
 :<- [::ed]
 (fn [db _]
   (:marks db))) ;;fixme ::items


(reg-sub
 ::cover
 :<- [::ed]
 (fn [db _]
   (-> db
       (select-keys (keys cover->db-map))
       (update-in [:marks]
                  #(map
                    (fn [[k m]] (select-keys m (keys mark->db-map)))
                    %)))))

(reg-sub
 ::size
 :<- [::ed]
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
 :<- [::size]
 (fn [[marks size] [_ id]]
   ;; convert relative to absolute xy
   (let [[x y] (get-in marks [id :pos])
         [w h] size]
     [(* x w)
      (* y h)])))


(reg-sub
 ::image-url
 :<- [::ed]
 (fn [db _]
   (:image-url db)))

#_(reg-sub
   :a-b-sub
   (fn [q-vec d-vec]
     [(subs/subscribe [:a-sub])
      (subs/subscribe [:b-sub])])
   (fn [[a b] [_]] {:a a :b b}))



;; load cover (eid)

