(ns coverton.fabric.events
  (:require [re-frame.core :refer [reg-event-fx reg-event-db]]
            [coverton.ed.events :as ed-evt :refer [cover-interceptors ed-interceptors]]
            [taoensso.timbre    :refer [info]]))



(reg-event-fx
 ::fabric->cover
 ed-interceptors
 (fn [{db :db} [canvas]]
   (let [size (or (get-in db [:cover :cover/fabric :size])
                  [(.getWidth canvas) (.getHeight canvas)])]

     {:dispatch-n [[::ed-evt/merge-cover
                    {:cover/fabric
                     {:size size
                      :json (js->clj (.toJSON canvas))
                      :svg  (->> {:width "100%"
                                  :height "100%"}
                                 clj->js
                                 (.toSVG canvas))}}]
                   
                   ;; after post-upload cover merge, fabric component
                   ;; has redrawn so we upload the cover with updated
                   ;; urls
                   (when (:uploading? db)
                     [::ed-evt/upload-cover
                      {:on-success [::upload-success]
                       :on-failure [::upload-failure]}])]})))




(reg-event-fx
 ::cover->db
 ed-interceptors
 (fn [{db :db} _]
   (merge
    {:db (assoc db :uploading? true)}

    (if-let [files (:files-to-upload db)]

      {:dispatch [::ed-evt/upload-files files
                  {:on-success [::ed-evt/merge-cover]
                   :on-failure [::upload-failure]}]}
     
      {:dispatch [::ed-evt/upload-cover
                  {:on-success [::upload-success]
                   :on-failure [::upload-failure]}]}))))




(reg-event-fx
 ::upload-success
 ed-interceptors
 (fn [{db :db} [resp]]
   (info "upload success")
   {:db (dissoc db :uploading? :files-to-upload)
    :dispatch [::ed-evt/merge-cover resp]}))




(reg-event-db
 ::upload-failure
 ed-interceptors
 (fn [db [resp]]
   (info "upload failed:" resp)
   (dissoc db :uploading?)))
