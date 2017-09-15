(ns coverton.index.views
  (:require [reagent.core  :as r]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [coverton.index.events :as evt]
            [coverton.index.subs   :as sub]
            [coverton.ed.views     :as ed]
            [coverton.components   :as cc]
            [coverton.ajax.events  :as evt-ajax]
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
               page  (subscribe [::sub/page])
               covers (subscribe [::sub/covers])
               active-cover (r/atom nil)]

    (condp = @page
      ;; Editor
      :ed [ed/editor {:cover (-> @active-cover
                                 (dissoc :cover-id))}]

      ;; Index
      [:div.index
       [cc/Button {:on-click #(do (reset! active-cover {})
                                  (evt/set-page :ed))}
        "New"]

       [cc/Button {:on-click #((dispatch-sync [::evt/initialize]))}
        "Refresh"]


       (when (and @(subscribe [::sub/authenticated?])
                  (= "dimovich" @(subscribe [::sub/user])))
         [cc/Button {:style {:position :fixed
                             :bottom 0 :right 0}
                     :on-click #(dispatch [::evt-ajax/request-auth {:uri "export-db"}])}
          "Export DB"])

       
       [auth-box]

       [:div.covers-container
        (for [cover @covers]
          ^{:key (:cover-id cover)}
          [cc/cover-block cover
           {:on-click #(do (reset! active-cover cover)
                           (evt/set-page :ed))}])]
       
       #_[:div {:class "motto vcenter"
                :style {:text-align :left}}
          [:img.logo {:src "assets/svg/logo.svg"}]
          [:p.text
           "a publishing platform for cover makers"
           [:br]
           "is coming soon."]]
       
       #_([:br] [:br]
          [:div {:style {:text-align :left
                         :font-size :18}}
           (str @(subscribe [::sub/db]))])])))
