(ns coverton.index.views
  (:require [reagent.core  :as r]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [ajax.core     :as ajax :refer [POST GET]]
            [coverton.index.events :as evt]
            [coverton.index.subs   :as sub]
            [coverton.ed.views     :as ed]
            [coverton.ed.events    :as ed-evt]
            [coverton.ed.subs      :as ed-sub]
            [coverton.util         :refer [info]]
            [coverton.db.schema    :refer [magic-id]]))

(enable-console-print!)

(def kcover (r/atom nil))

(def Button (r/adapt-react-class
             (goog.object/getValueByKeys js/window "deps" "semui" "Button")))


(defn save-cover [cover]
  (POST "/save-cover" {:handler (fn [res]
                                  (ed-evt/update-cover-id (:cover-id res))
                                  (info res))
                       :error-handler #(.log js/console (str %))
                       :params {:cover cover}}))


(defn get-cover [id cb]
  (POST "/get-cover" {:handler (fn [cover]
                                 (info cover)
                                 (cb cover))
                      :error-handler #(info %)
                      :params {:id id}}))


(defn index []
  (r/with-let [ ;;_            (dispatch-sync [::evt/initialize])
               active-panel (subscribe [::sub/active-panel])
               cover        (subscribe [::ed-sub/cover])
               state        (r/atom {})
               set-cover    #(swap! state assoc :cover %)]
    [:div.index
     [Button {:on-click #(dispatch [::evt/set-active-panel :index])}
      "Index"]
     [Button {:on-click #(dispatch [::evt/set-active-panel :ed])}
      "Editor"]
     [Button {:on-click #(save-cover @cover)}
      "Save Cover"]
     [Button {:on-click #(get-cover magic-id set-cover)}
      "Load Cover"]

     #_(when @cover ;;autosave...
         (save-cover @cover))
     
     (condp = @active-panel
       :ed [ed/editor {:cover (:cover @state)}] ;;^{:key @ed-t}
       
       [:div {:class "motto vcenter"
              :style {:text-align :left}}
        [:img.logo {:src "assets/svg/logo.svg"}]
        [:p.text
         "a publishing platform for cover makers"
         [:br]
         "is coming soon."]])]))
