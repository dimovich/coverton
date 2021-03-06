(ns coverton.index.views
  (:require [reagent.core  :as r]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [coverton.index.events :as evt]
            [coverton.index.subs   :as sub]
            [coverton.fabric.views :as fab]
            [coverton.ed.subs      :as ed-sub]
            [coverton.ed.events    :as ed-evt]
            [coverton.components   :as cc]
            [coverton.ajax.events  :as ajax-evt]
            [taoensso.timbre       :refer [info]]
            [coverton.validations  :as v]))



(defn search-tag [opts tag]
  [:div.search-tag
   [:span.helper-valign]
   [:span.search-tag-text {} tag]
   [:img.search-tag-close
    (merge {:src "assets/svg/x.svg"}
           opts)]])



(defn search-box [{:keys [tags]}]
  (r/with-let [text (r/atom nil)
               add-tag (fn [_]
                         (when-not (empty? @text)
                           (dispatch [::evt/update :search-tags
                                      #(-> (or %1 [])
                                           (conj %2))
                                      @text])
                           (reset! text nil)))
               
               remove-tag (fn [idx]
                            (dispatch [::evt/update
                                       :search-tags
                                       #(vec (concat
                                              (subvec % 0 idx)
                                              (subvec % (inc idx))))]))
               pop-tag #(dispatch
                         [::evt/update
                          :search-tags (comp vec butlast)])]

    [:div.search-bar

     ;; Tags
     (map-indexed
      (fn [idx tag]
        [search-tag {:on-click #(remove-tag idx)
                     :key idx}
         tag])
      tags)

     ;; Search input
     [cc/editable :input {:state text
                          :auto-focus true
                          :class :search-input-field
                          :on-key-down (fn [e]
                                         (condp = (.. e -key)
                                           "Enter"     (add-tag)
                                           "Backspace" (when (empty? @text)
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
                           ;;:type :email
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
      "Log in"])))




(def page->header {:index #{:logo :motto :request-invite :auth :new}
                   :request-invite #{:logo :motto}
                   :fabric #{:logo :auth}})



(defn header [page]
  (r/with-let [show-login?  (r/atom false)
               authenticated? (subscribe [::sub/authenticated?])
               request-sent?  (subscribe [::sub/key :request-invite-sent])
               on-click-logo #(do (evt/set-page :index)
                                  (reset! show-login? false)
                                  (dispatch [::evt/refresh]))]

    (let [els (get page->header page)]
      
      [:div.header
       (when (:logo els)
         [:span.logo-wrap.clickable
          {:on-click on-click-logo}
          [:img.logo {:src "assets/svg/logo.svg"}]
          (when (:motto els) [:span.logo-info
                              "a publishing platform for cover makers."])])
        

       (apply cc/menu
              (cond
                @authenticated?
                [(if (:new els)
                   [:a {:on-click
                        #(do (ed-evt/initialize)
                             (evt/set-page :fabric))}
                    "N E W"])
                 [:a {:on-click
                      #(do (dispatch [::evt/logout])
                           (reset! show-login? false))}
                  "Log out"]]
                @show-login? [[login-form]]
                
                :default
                [(if (and (:request-invite els)
                          (not @request-sent?))
                   [:a {:on-click #(evt/set-page :request-invite)}
                    "request invitation"]) ;;fixme lone Log In sticks to the top?
                 (when (:auth els)
                   [:a {:on-click #(reset! show-login? true)}
                    "Log in"])]))])))






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
                             :on-change #(validate :email)
                             :error (:email @errors)}]

        [:p "Tell us about yourself and your work:"]
        [cc/editable :textarea {:state (r/cursor state [:story])
                                :on-change #(validate :story)
                                :error (:story @errors)}]

        [:button.clickable {:on-click send}
         [:a "Send Application"]]]))))





(defn gen-cover-box-css [coll n]
  
  (let [all    (count coll)
        nend   (mod all n)
        nstart (- all nend)]
    
    (concat
     (repeat nstart :div.cover-block-wrap)
     (repeat nend   :div.cover-block-wrap-end))))




(defn index-page []
  (let [page-scroll (subscribe [::sub/key :page-scroll])
        covers      (subscribe [::sub/covers])
        search-tags (subscribe [::sub/search-tags])]
    
    (r/create-class
     {:component-did-mount
      (fn [this]
        (window.scroll 0 @page-scroll)
        (dispatch [::evt/assoc :page-scroll 0]))
      
      :reagent-render
      (fn []
        (when @search-tags
          (dispatch [::evt/refresh]))
        
        [:div.index
         [search-box {:tags @search-tags}]
       
         [:div.covers-container
          (map
           (fn [cover css]

             ^{:key (:cover/id cover)}
             [css
              [cc/cover-block
               {:on-click #(do (dispatch [::evt/assoc :page-scroll window.scrollY])
                               (ed-evt/initialize cover)
                               (evt/set-page :fabric))}
               cover]
              
              [:div.cover-block-info
               [:div.cover-block-author (:cover/author cover)]]])
           
           @covers
           (gen-cover-box-css @covers 3))]])})))




(defn index []
  (r/with-let [_    (dispatch-sync [::evt/initialize])
               page (subscribe [::sub/page])]
    [:div
     [header @page]
     
     [:div.page
      (condp = @page
        :request-invite [request-invite]
        ;; Editor
        :fabric [fab/editor]
        
        ;; Index
        [index-page])]]))
