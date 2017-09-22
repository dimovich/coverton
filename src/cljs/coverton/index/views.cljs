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
  (r/with-let [state (r/atom {:username "" :password ""})
               login #(dispatch [::evt/login @state])] 
    [:form
     [:input {:placeholder "username:"
              :value (:username @state)
              :on-change #(swap! state assoc :username (.. % -target -value))}]
     
     [:input {:type :password
              :placeholder "password:"
              :value (:password @state)
              :on-key-up (fn [e]
                             (condp = (.. e -key)
                               "Enter" (login)
                               false))
              :on-change #(swap! state assoc :password (.. % -target -value))}]
     
     [:a.menu {:on-click login}
      "login"]]))



(defn auth-box []
  [:div.auth-box
   (if @(subscribe [::sub/authenticated?])
     [:a.menu {:on-click #(dispatch [::evt/logout])}
      "logout"]
       
     [login-form])])




(defn index []
  (r/with-let [_     (dispatch-sync [::evt/initialize])
               page  (subscribe [::sub/page])
               covers (subscribe [::sub/covers])
               active-cover (r/atom nil)
               authenticated? (subscribe [::sub/authenticated?])]

    (condp = @page
      ;; Editor
      :ed [ed/editor {:cover (-> @active-cover
                                 (dissoc :cover-id))}]

      ;; Index
      [:div.index
       (cc/menu
        (when @authenticated?
          [:a {:on-click #(do (reset! active-cover {})
                              (evt/set-page :ed))}
           "N E W"]))

       [:div.header
        
        [:span {:style {:left 0}}
         [:img.logo {:src "assets/svg/logo.svg"}]
         "a publishing platform for cover makers."]

        [:span {:style {:float :right}}
         (cc/menu
          [:a "request invitation"]
          [:a "log in"])]]


       [:div.search-bar
        [cc/react-tags {:tags [{:id 1 :text "Hello"}]}]]
       
       
       
       #_[auth-box]

       [:div.covers-container
        (for [cover @covers]
          ^{:key (:cover-id cover)}
          [cc/cover-block cover
           {:on-click #(do (reset! active-cover cover)
                           (evt/set-page :ed))}])]
       
       #_([:div {:class "motto vcenter"
                 :style {:text-align :left}}
           [:img.logo {:src "assets/svg/logo.svg"}]
           [:p.text
            "a publishing platform for cover makers"
            [:br]
            "is coming soon."]])
       
       #_([:br] [:br]
          [:div {:style {:text-align :left
                         :font-size :18}}
           (str @(subscribe [::sub/db]))])])))
