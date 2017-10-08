(ns coverton.index.views
  (:require [reagent.core  :as r]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [coverton.index.events :as evt]
            [coverton.index.subs   :as sub]
            [coverton.ed.views     :as ed]
            [coverton.components   :as cc]
            [coverton.ajax.events  :as ajax-evt]
            [taoensso.timbre :refer-macros [info]]
            [coverton.validations  :as v]))



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
     [cc/editable :input {:state (r/cursor state [:text])
                          :auto-focus true
                          :class :search-input-field
                          :on-key-down (fn [e]
                                         (condp = (.. e -key)
                                           "Enter"     (add-tag)
                                           "Backspace" (when (empty? @state)
                                                         (pop-tag))
                                           false))}]

     [:img.search-image {:src "assets/svg/search.svg"}]]))




(defn login-form []
  (r/with-let [state (r/atom {:email "" :password ""})
               errors (r/atom nil)
               validate (v/validator state errors)
               login #(when (empty? (validate :email :password))
                        (evt/login @state))] 
    (cc/menu
     [:span
      [cc/editable :input {:placeholder "email:"
                           :auto-focus true
                           :state (r/cursor state [:email])
                           :on-change #(validate :email)
                           :error (:email @errors)}]
     

      [cc/editable :input {:type :password
                           :placeholder "password:"
                           :state (r/cursor state [:password])
                           :error (:password @errors)
                           :on-change #(validate :password)
                           :on-key-up (fn [e]
                                        (condp = (.. e -key)
                                          "Enter" (login)
                                          false))}]]
     [:a {:on-click login}
      "log in"])))




(defn header [page]
  (r/with-let [show-login?  (r/atom false)
               authenticated? (subscribe [::sub/authenticated?])
               request-sent?  (subscribe [::sub/key :request-invite-sent])]

    (let [els (-> {:index #{:logo :request-invite :auth}
                   :ed    #{:logo :request-invite :auth}
                   :request-invite #{:logo}}
                  (get page))]
      
      [:div.header
       (when (:logo els)
         [:span.clickable {:style {:left 0}
                           :on-click #(do (evt/set-page :index)
                                          (reset! show-login? false))}
      
          [:img.logo {:src "assets/svg/logo.svg"}]
          [:span.logo-name "Coverton"]
          "a publishing platform for cover makers."])
        

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
                 :default [(when (and (:request-invite els)
                                      (not @request-sent?))
                             [:a {:on-click #(evt/set-page :request-invite)} "request invitation"])
                           (when (:auth els)
                             [:a {:on-click #(reset! show-login? true)} "log in"])]))]])))




(defn request-invite []
  (r/with-let [state    (r/atom {:email "" :story ""})
               errors   (r/atom nil)
               sent?    (subscribe [::sub/key :request-invite-sent])
               validate (v/validator state errors)
               send     #(when (empty? (validate :email :story :request-invite))
                           (evt/request-invite @state))]
    
    (into
     [:div.request-invite]
     (if @sent?
       [[:p "Application sent."]]
       [[:p
         "To apply for a Coverton account use the form below." [:br]
         "For support visit " [:a "support.coverton.co"]]
        [:hr]
        
        [:p "Your email address:"]
        [cc/editable :input {:auto-focus true
                             :state (r/cursor state [:email])
                             ;;:on-blur #(validate :email)
                             :on-change #(validate :email)
                             :error (:email @errors)}]

        [:p "Tell us about yourself and your work:"]
        [cc/editable :textarea {:state (r/cursor state [:story])
                                ;;:on-blur #(validate :story)
                                :on-change #(validate :story)
                                :error (:story @errors)}]

        [:button.clickable {:on-click send}
         [:a "Send Application"]]]))))




(defn index []
  (r/with-let [_      (dispatch-sync [::evt/initialize])
               page   (subscribe [::sub/page])
               covers (subscribe [::sub/covers])
               search-tags (subscribe [::sub/search-tags])]
    
    [:div
     [header @page]
     
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
