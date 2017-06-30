(ns coverton.index.views
  (:require [reagent.core  :as r]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [ajax.core     :as ajax :refer [POST]]
            [coverton.index.events :as evt]
            [coverton.index.subs   :as sub]
            [coverton.ed.views     :as ed]
            [coverton.ed.subs      :as ed-sub]))


(enable-console-print!)


(def Button (r/adapt-react-class (aget js/window "deps" "semui" "Button")))


(defn handle-save-cover [e]
  (let [cover @(subscribe [::ed-sub/cover])]
    (POST "/add-cover" {:headers {"Accept" "application/transit+json"}
                        :handler (fn [res]
                                   (println res))
                        :error-handler #(.log js/console (str %))
                        :params {:cover cover}})))



(defn index []
  (r/with-let [ ;;_            (dispatch-sync [::evt/initialize])
               active-panel (subscribe [::sub/active-panel])]
    [:div.index
     [Button {:on-click #(dispatch [::evt/set-active-panel :index])}
      "Index"]
     [Button {:on-click #(dispatch [::evt/set-active-panel :ed])}
      "Editor"]
     [Button {:on-click handle-save-cover}
      "Save cover"]
     
     (condp = @active-panel
       :ed [ed/editor]
       
       [:div {:class "content vcenter"
              :style {:text-align :left}}
        [:img.logo {:src "assets/svg/logo.svg"}]
        [:p.text
         "a publishing platform for cover makers"
         [:br]
         "is coming soon."]])]))
