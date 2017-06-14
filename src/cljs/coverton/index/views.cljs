(ns coverton.index.views
  (:require [reagent.core  :as r]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [coverton.index.events :as evt]
            [coverton.index.subs   :as sub]
            [coverton.ed.views     :as ed]))


(def sem-button :div;;(r/adapt-react-class (aget js/window "deps" "semui-button"))
  )



(defn index []
  (r/with-let [;;_            (dispatch-sync [::evt/initialize])
               active-panel (subscribe [::sub/active-panel])]
    [:div.index
     [sem-button {:on-click #(dispatch [::evt/set-active-panel :index])}
      "Index"]
     [sem-button {:on-click #(dispatch [::evt/set-active-panel :ed])}
      "Editor"]
     
     (condp = @active-panel
       :ed [ed/editor]
       
       [:div {:class "content vcenter"}
        [:img.logo {:src "assets/svg/logo.svg"}]
        [:p.text
         "a publishing platform for cover makers"
         [:br]
         "is coming soon."]])]))
