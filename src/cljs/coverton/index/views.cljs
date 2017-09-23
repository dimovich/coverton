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



(defn search-tag [opts tag]
  [:div.search-tag-box
   [:span.search-tag {} tag]
   [:img.search-tag-close (merge {:src "assets/svg/x.svg"}
                                 opts)]])



(defn search-box [{:keys [tags]}]
  (r/with-let [state (r/atom nil)
               add-tag (fn [_]
                         (when-not (empty? @state)
                           (dispatch [::evt/update
                                      :search-tags
                                      #(-> (or %1 [])
                                           (conj %2))
                                      @state])
                           (reset! state nil)))
               
               remove-tag (fn [idx]
                            (dispatch [::evt/update
                                       :search-tags
                                       #(vec (concat (subvec % 0 idx)
                                                     (subvec % (inc idx))))]))
               pop-tag #(dispatch [::evt/update
                                   :search-tags (comp vec butlast)])]

    [:div.search-bar

     (map-indexed
      (fn [idx tag]
        ^{:key idx}
        [search-tag {:on-click #(remove-tag idx)} tag])
      tags)

     [:img.search-image {:src "assets/svg/search.svg"}]
     
     [:span.search-input
      [:input.search-input-field {:value @state
                                  :on-change #(reset! state (.. % -target -value))
                                  :on-key-down (fn [e]
                                                 (condp = (.. e -key)
                                                   "Enter"     (add-tag)
                                                   "Backspace" (when (empty? @state)
                                                                 (pop-tag))
                                                   false))}]]]))




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
               authenticated? (subscribe [::sub/authenticated?])
               search-tags (subscribe [::sub/search-tags])]
    
    (condp = @page
      ;; Editor
      :ed [ed/editor {:cover (-> @active-cover
                                 (dissoc :cover-id))}]
      
      ;; Index
      [:div.index
       (cc/menu
        (when true ;;@authenticated?
          ))

       [:div.header
        
        [:span {:style {:left 0}}
         [:img.logo {:src "assets/svg/logo.svg"}]
         "a publishing platform for cover makers."]
        
        [:span {:style {:float :right}}
         (cc/menu
          [:a "request invitation"]
          (if @authenticated?
            [:a {:on-click #(dispatch [::evt/logout])} "log out"]
            [:a {:on-click #(dispatch [::evt/login {:username "dimovich"
                                                    :password ""}])}
             "log in"]))]]


       (when @authenticated?
         [:a {:style {:color "#1a1a1a"
                      :font-size "0.8em"}
              :on-click #(do (reset! active-cover {})
                             (evt/set-page :ed))}
          "N E W"])
       

              
       [search-box {:tags @search-tags}]

       (when @search-tags
         (dispatch [::evt/refresh]))
       
       
       #_[auth-box]

       [:div.covers-container
        (for [cover @covers]
          ^{:key (:cover-id cover)}
          [cc/cover-block cover
           {:on-click #(do (reset! active-cover cover)
                           (evt/set-page :ed))}])]
       
       #_([:br] [:br]
          [:div {:style {:text-align :left
                         :font-size :18}}
           (str @(subscribe [::sub/db]))])])))
