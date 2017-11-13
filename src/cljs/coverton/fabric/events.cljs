(ns coverton.fabric.events
  (:require [re-frame.core      :refer [reg-event-fx reg-event-db dispatch]]
            [taoensso.timbre    :refer [info]]
            [coverton.ed.events :as ed-evt :refer [cover-interceptors ed-interceptors]]))




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
     (info json)
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
            (update-in [:fabric :snapshot-idx]
                       (fn [_] 0)))}))




(reg-event-db
 ::update-urls
 ed-interceptors
 (fn [db [urls]]
   (let [images (:images db)
         pending (atom (count images))
         canvas (get-in db [:fabric :canvas])]

     ;; update image sources (async, so make sure we keep track when
     ;; they finish loading)
     (doseq [[id url] urls]
       (.setSrc (get images id) url
                #(swap! pending dec)))

     ;; wait for canvas to update, and then upload cover
     ;; (is there a better way?)
     ;; https://github.com/vimsical/re-frame-utils/blob/master/src/vimsical/re_frame/fx/track.cljc
     
     (js/setTimeout
      (fn check []
        (if (< 0 @pending)
          (js/setTimeout check 2)
          (dispatch [::fabric->cover
                     [::ed-evt/upload-cover
                      {:on-success [::upload-success]
                       :on-failure [::upload-failure]}]])))
      5)
     
     (dissoc db :files-to-upload))))




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
                  {:on-success [::update-urls]
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
