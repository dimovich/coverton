(ns coverton.fabric.events
  (:require [re-frame.core      :refer [reg-event-fx reg-event-db dispatch path trim-v]]
            [taoensso.timbre    :refer [info]]
            [coverton.ed.events :as ed-evt :refer [cover-interceptors ed-interceptors]]))


(def pending-interceptors [(path [:ed :pending]) trim-v])


;;
;; save fabric data to app-db, and then run "next-op" if specified
;;
(reg-event-fx
 ::fabric->cover
 ed-interceptors
 (fn [{db :db} [next-op]]
   (let [canvas (get-in db [:fabric :canvas])
         size (or (get-in db [:cover :cover/fabric :size])
                  [(.getWidth canvas) (.getHeight canvas)])]

     {:dispatch-n [[::ed-evt/merge-cover
                    {:cover/fabric
                     {:size size
                      :json (js->clj (.toJSON canvas))
                      :svg  (->> {:width "100%"
                                  :height "100%"}
                                 clj->js
                                 (.toSVG canvas))}}]

                   [::snapshot]

                   (when next-op
                     next-op)]})))



(reg-event-db
 ::undo
 ed-interceptors
 (fn [db _]
   (let [canvas (get-in db [:fabric :canvas])
         idx (get-in db [:fabric :snapshot-idx])
         snapshot (-> db
                      (get-in [:fabric :snapshots])
                      rest
                      (nth idx))
         json (clj->js (:json snapshot))]
     (.clear canvas)
     (.loadFromJSON canvas json #(.renderAll canvas))
     (update-in db [:fabric :snapshot-idx] inc))))



(reg-event-fx
 ::snapshot
 ed-interceptors
 (fn [{db :db} _]
   {:db (-> db
            ;;save fabric json
            (update-in [:fabric :snapshots]
                       conj (get-in db [:cover :cover/fabric]))
            ;;reset current snapshot index
            (assoc-in [:fabric :snapshot-idx] 0))}))



;; we're gonna do some pending async canvas operations, so keep track
;; when they finish and then dispatch a "next-op".
;;
(reg-event-db
 ::start-pending
 pending-interceptors
 (fn [db [counter next-op]]
   {:counter counter
    :next-op next-op}))


(reg-event-fx
 ::update-pending
 pending-interceptors
 (fn [{db :db} _]
   (let [counter (-> (:counter db) dec)]
     (if (< 0 counter)
       {:db (assoc db :counter counter)}
       {:db nil
        :dispatch (:next-op db)}))))



(reg-event-fx
 ::upload-files-success
 ed-interceptors
 (fn [{db :db} [urls]]
   {:db (dissoc db :files-to-upload)
    :dispatch-n [[::start-pending
                  (count urls)
                  [::fabric->cover
                   [::ed-evt/upload-cover
                    {:on-success [::upload-success]
                     :on-failure [::upload-failure]}]]]

                 [::update-urls urls]]}))



(reg-event-fx
 ::update-urls
 ed-interceptors
 (fn [{db :db} [urls]]
   (info "updating urls...")
   (let [images (:images db)
         canvas (get-in db [:fabric :canvas])]
     ;; update image sources (async, so make sure we keep track when
     ;; they finish loading)
     (doseq [[id url] urls]
       (.setSrc (get images id) url
                #(dispatch [::update-pending]))))))



(reg-event-fx
 ::cover->db
 ed-interceptors
 (fn [{db :db} _]
   (merge
    {:db (assoc db :uploading? true)}

    (if-let [files (:files-to-upload db)]
      ;;got some files to upload, so do that, then merge the server
      ;;response back into the cover; the updated urls will trigger a
      ;;redraw which will save the cover and then upload it to the
      ;;server.
      {:dispatch [::ed-evt/upload-files files
                  {:on-success [::upload-files-success]
                   :on-failure [::upload-failure]}]}

      ;;no files to upload, so just upload the cover as is
      {:dispatch [::ed-evt/upload-cover
                  {:on-success [::upload-success]
                   :on-failure [::upload-failure]}]}))))




(reg-event-fx
 ::upload-success
 ed-interceptors
 (fn [{db :db} [resp]]
   (info "upload success")
   {:db (dissoc db :uploading?)
    :dispatch [::ed-evt/merge-cover resp]}))




(reg-event-db
 ::upload-failure
 ed-interceptors
 (fn [db [resp]]
   (info "upload failed:" resp)
   (dissoc db :uploading?)))
