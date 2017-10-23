(ns coverton.ed.views
  (:require [reagent.core  :as r]
            [dommy.core    :as d  :refer-macros [sel1]]
            [re-frame.core :as rf :refer [subscribe dispatch dispatch-sync]]
            [ajax.core     :as ajax :refer [POST GET]]
            [coverton.components  :as cc]
            [coverton.ed.events   :as evt]
            [coverton.ed.subs     :as sub]
            [coverton.ajax.events :as ajax-evt]
            [taoensso.timbre :refer-macros [info]]
            [coverton.index.events :as evt-index]
            [coverton.index.subs   :as sub-index]))


;; use center of element for position
;; how to recover? (in



(defn handle-remove-mark [e]
  (let [text (.. e -target -value)
        id   (.. e -target -id)]
    (when (empty? text)
      (evt/remove-mark id))))




(defn on-click-add-mark [parent e]
  (let [[w h] @(subscribe [::sub/size])
        rect  (.. parent getBoundingClientRect)
        rx    (.. rect -left)
        ry    (.. rect -top)
        x     (- (.. e -clientX) rx)
        y     (- (.. e -clientY) ry)]
    (evt/add-mark [(/ x w) (/ y h)])))



(defn image [{url :url}]
  (r/with-let [this        (r/current-component)
               update-size (fn [_]
                             (let [el (r/dom-node this)
                                   w  (.. el getBoundingClientRect -width)
                                   h  (.. el getBoundingClientRect -height)]
                               (evt/set-size [w h])))]
    
    [:img.editor-img {:on-click #(on-click-add-mark (r/dom-node this) %)
                      :on-load  update-size ;; image loads later than component mounts
                      :src      url}]))



(defn editor-img []
  (let [ids       (subscribe [::sub/mark-ids])
        size      (subscribe [::sub/size])
        image-url (subscribe [::sub/image-url])]

    (r/create-class
     {:display-name "editor-img"

      :component-did-mount
      (fn [this])

      :reagent-render
      (fn []
        (into
         [:div.editor-img-wrap {:on-blur handle-remove-mark}

          [image {:url @image-url}]
          
          #_(when @size
              (for [id @ids]
                ^{:key id} [mark {:id id}]))]))})))




;; take ESC from dimmer
(defn editor [{:keys [cover]}]
  (r/with-let [_       (evt/initialize cover)
               dimmer  (subscribe [::sub/dimmer])
               ed-t    (subscribe [::sub/t])
               authenticated? (subscribe [::sub-index/authenticated?])]

    ^{:key @ed-t} ;;forces re-mount when cover is re-initialized
    [:div.editor
     
     [:div.header {:style {:text-align :center
                           :margin "0.5em auto"}}
      (cc/menu
       [cc/image-picker]
      
       (when @authenticated?
         [:a {:on-click #(evt/save-cover)}
          "save"])
      
       [:a {:on-click #(do (evt-index/set-page :index)
                           (evt-index/refresh))}
        "close"])]

     (condp = @dimmer
       :font-picker [cc/font-picker]
        
       [editor-img])]))

