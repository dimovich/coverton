(ns coverton.index.views
  (:require [reagent.core  :as r]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [coverton.index.events :as evt]
            [coverton.index.subs   :as sub]
            [coverton.ed.views     :as ed]
            [coverton.ed.subs      :as ed-sub]
            [coverton.components   :as cc]
            [coverton.util         :refer [info]]
            [coverton.db.schema    :refer [magic-id]]))



(defn login-form []
  (r/with-let [state (r/atom {:user "" :pass ""})] 
    [:form
     [:input {:type :text
              :placeholder "Username:"
              :value (:user @state)
              :on-change #(swap! state assoc :user (.. % -target -value))}]
     
     [:input {:type :password
              :placeholder "Password:"
              :value (:pass @state)
              :on-change #(swap! state assoc :pass (.. % -target -value))}]
     
     [:input {:type "button"
              :value "Submit"
              :on-click #(dispatch [::evt/login @state])}]]))



(defn auth-box []
  (if @(subscribe [::sub/authenticated?])
    [:input {:type :button
             :value "Logout"
             :on-click #(dispatch [::evt/logout])}]
       
    [login-form]))




(defn index []
  (r/with-let [_            (dispatch-sync [::evt/initialize])
               active-panel (subscribe [::sub/active-panel])]

    (condp = @active-panel
      ;; Editor
      :ed [ed/editor {:cover {}}]

      ;; Index
      [:div.index
       [cc/Button {:on-click #(evt/push-panel :ed)}
        "Editor"]
       
       [auth-box]
       
       [:div {:class "motto vcenter"
              :style {:text-align :left}}
        [:img.logo {:src "assets/svg/logo.svg"}]
        [:p.text
         "a publishing platform for cover makers"
         [:br]
         "is coming soon."]]
       
       [:br] [:br]
       [:div (str @(subscribe [::sub/db]))]])))
