(ns coverton.index.views
  (:require [reagent.core  :as r]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [coverton.index.events :as evt]
            [coverton.index.subs   :as sub]
            [coverton.ed.views     :as ed]))



(defn index []
  (r/with-let [_            (dispatch-sync [::evt/initialize])
               active-panel (subscribe [::sub/active-panel])]
    [:div.index
     [:button {:on-click #(dispatch [::evt/set-active-panel :index])}
      "Index"]
     [:button {:on-click #(dispatch [::evt/set-active-panel :ed])}
      "Editor"]
     
     (condp = @active-panel
       :ed [ed/editor]
       [:p.text [:br] "INDEX"])]))
