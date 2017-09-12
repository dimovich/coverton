(ns coverton.index.views
  (:require [reagent.core  :as r]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [coverton.index.events :as evt]
            [coverton.index.subs   :as sub]
            [coverton.ed.views     :as ed]
            [coverton.components   :as cc]
            [coverton.db.schema    :refer [magic-id]]
            [taoensso.timbre :refer-macros [info]]))



(defn login-form []
  (r/with-let [state (r/atom {:username "" :password ""})] 
    [:form
     [cc/Input {:placeholder "Username:"
                :value (:username @state)
                :on-change #(swap! state assoc :username (.. % -target -value))}]
     
     [cc/Input {:type :password
                :placeholder "Password:"
                :value (:password @state)
                :on-change #(swap! state assoc :password (.. % -target -value))}]
     
     [cc/Button {:type :button
                 :on-click #(dispatch [::evt/login @state])}
      "Login"]]))



(defn auth-box []
  [:div.auth-box
   (if @(subscribe [::sub/authenticated?])
     [cc/Button {:on-click #(dispatch [::evt/logout])}
      "Logout"]
       
     [login-form])])




(defn index []
  (r/with-let [_     (dispatch-sync [::evt/initialize])
               page  (subscribe [::sub/page])]

    (condp = @page
      ;; Editor
      :ed [ed/editor {:cover {}}]

      ;; Index
      [:div.index
       [cc/Button {:on-click #(evt/set-page :ed)}
        "Editor"]
       
       [auth-box]
       
       [:div {:class "motto vcenter"
              :style {:text-align :left}}
        [:img.logo {:src "assets/svg/logo.svg"}]
        [:p.text
         "a publishing platform for cover makers"
         [:br]
         "is coming soon."]]
       
       #_( [:br] [:br]
          [:div {:style {:text-align :left
                         :font-size :18}}
           (str @(subscribe [::sub/db]))])])))
