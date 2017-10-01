(ns coverton.index.views
  (:require [reagent.core  :as r]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [coverton.index.events :as evt]
            [coverton.index.subs   :as sub]
            [coverton.ed.views     :as ed]
            [coverton.components   :as cc]
            [coverton.ajax.events  :as ajax-evt]
            [taoensso.timbre :refer-macros [info]]))



(defn search-tag [opts tag]
  [:div.search-tag
   [:span.helper-valign]
   [:span.search-tag-text {} tag]
   [:img.search-tag-close
    (merge {:src "assets/svg/x.svg"}
           opts)]])



(defn search-box [{:keys [tags]}]
  (r/with-let [state (r/atom nil)
               add-tag (fn [_]
                         (when-not (empty? @state)
                           (dispatch [::evt/update
                                      :search-tags
                                      #(-> (or %1 [])
                                           (conj %2))
                                      (:text @state)])
                           (reset! state nil)))
               
               remove-tag (fn [idx]
                            (dispatch [::evt/update
                                       :search-tags
                                       #(vec (concat (subvec % 0 idx)
                                                     (subvec % (inc idx))))]))
               pop-tag #(dispatch [::evt/update
                                   :search-tags (comp vec butlast)])]

    [:div.search-bar

     ;; Tags
     (map-indexed
      (fn [idx tag]
        ^{:key idx}
        [search-tag {:on-click #(remove-tag idx)} tag])
      tags)

     ;; Search input
     [cc/component :input {:state (r/cursor state [:text])
                           :class :search-input-field
                           :on-key-down (fn [e]
                                          (condp = (.. e -key)
                                            "Enter"     (add-tag)
                                            "Backspace" (when (empty? @state)
                                                          (pop-tag))
                                            false))}]

     [:img.search-image {:src "assets/svg/search.svg"}]]))




(defn login-form []
  (r/with-let [state (r/atom {:username "" :password ""})
               login #(dispatch [::evt/login @state])] 
    (cc/menu
     [:span
      [cc/component :input {:placeholder "username:"
                            :auto-focus true
                            :state (r/cursor state [:username])}]
     

      [cc/component :input {:type :password
                            :placeholder "password:"
                            :state (r/cursor state [:password])
                            :on-key-up (fn [e]
                                         (condp = (.. e -key)
                                           "Enter" (login)
                                           false))}]]
     [:a {:on-click login}
      "log in"])))




(defn header []
  (r/with-let [show-login?  (r/atom false)
               authenticated? (subscribe [::sub/authenticated?])]

    [:div.header
     [:span.clickable {:style {:left 0}
                       :on-click #(evt/set-page :index)}
      
      [:img.logo {:src "assets/svg/logo.svg"}]
      [:span.logo-name "Coverton"]
      "a publishing platform for cover makers."]
        

     [:span {:style {:float :right}}
      (apply cc/menu
             (cond
               @authenticated? [[:a {:on-click #(do (evt/set-active-cover {})
                                                    (evt/set-page :ed))}
                                 "N E W"]
                                [:a {:on-click #(do (dispatch [::evt/logout])
                                                    (reset! show-login? false))}
                                 "log out"]]
               @show-login? [[login-form]]
               :default [[:a {:on-click #(evt/set-page :request-invite)} "request invitation"]
                         [:a {:on-click #(reset! show-login? true)} "log in"]]))]]))




(defn request-invite []
  (r/with-let [state (r/atom nil)
               sent  (subscribe [::sub/key :request-invite-sent])]
    (into
     [:div.request-invite]
     (if @sent
       [[:p "Application sent."]]
       [[:p
         "To apply for a Coverton account use the form below." [:br]
         "For support visit " [:a "support.coverton.co"]]
        [:hr]
        [:p "Your email address:"]
        [cc/component :input {:auto-focus true
                              :state (r/cursor state [:email])}]

        [:p "Tell us about yourself and your work:"]
        [cc/component :textarea {:state (r/cursor state [:story])}]

        [:button.clickable {:on-click
                            #(dispatch [::ajax-evt/request
                                        {:method :post
                                         :uri "/request-invite"
                                         :params @state
                                         :on-success [::evt/merge]}])}
         [:a "Send Application"]]]))))




(defn index []
  (r/with-let [_      (dispatch-sync [::evt/initialize])
               page   (subscribe [::sub/page])
               covers (subscribe [::sub/covers])
               search-tags (subscribe [::sub/search-tags])]
    
    [:div
     [header]
     
     [:div.page
      (condp = @page
        :request-invite [request-invite]
        ;; Editor
        :ed [ed/editor {:cover (-> @(subscribe [::sub/active-cover])
                                   (dissoc :cover/id))}]
      
        ;; Index
        [:div.index
         [search-box {:tags @search-tags}]

         (when @search-tags
           (dispatch [::evt/refresh]))
       
       
         [:div.covers-container
          (for [cover @covers]
            ^{:key (:cover/id cover)}
            [:div.cover-block-box
             [cc/cover-block cover {:on-click #(do (evt/set-active-cover cover)
                                                   (evt/set-page :ed))}]
             [:div.cover-block-info
              [:div.cover-block-author (:cover/author cover)]
              ;;[:div.cover-block-tags   "tags:"]
              ]])]])

     
      #_([:br] [:br]
         [:div {:style {:text-align :left
                        :font-size :18}}
          (str @(subscribe [::sub/db]))])]]))
