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

(defn mark [{id :id}]
  (let [text  (subscribe [::sub/mark-text id])
        pos   (subscribe [::sub/mark-pos  id])
        color (subscribe [::sub/color     id])
        font-family  (subscribe [::sub/mark-font-family id])
        font-size    (subscribe [::sub/mark-font-size   id])
        read-only?   (subscribe [::sub/mark-read-only?  id])
        child-ref    (atom nil)
        this         (r/current-component)
        ;;initial click coords
        [x y]        @pos]

    (r/create-class
     {:display-name "mark"
      :reagent-render
      (fn []
        [:div.mark {:style {:left x :top y}}
         
         [cc/draggable {:update-fn #(evt/set-pos id %)
                        ;;we get deltas, so we need the initial coords
                        :start-pos [x y]}

          ;; fixme: move toolbox to inner
          [cc/toolbox {:id id
                       :ref child-ref}]
         
          [cc/resizable {:font-size  @font-size
                         :child-ref child-ref
                         :update-fn  #(evt/set-font-size id %)}
          
           [cc/autosize-input {:id          id
                               :set-ref     #(reset! child-ref %)
                               :key         :input
                               :text        @text
                               :color       @color
                               :font-family @font-family
                               :read-only?  @read-only?
                               :update-fn   #(evt/set-text id %)}]]]])})))



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
          
          (when @size
            (for [id @ids]
              ^{:key id} [mark {:id id}]))]))})))




(defn form-data [id]
  (when-let [file (some->
                   (sel1 id)
                   .-files
                   (aget 0))]
    (doto
        (js/FormData.)
        (.append "file" file))))




(defn save-cover [cover]
  (if-let [file (form-data :#image-input)] ;;todo: check if already uploaded
    (dispatch [::evt/upload-file file
               {:on-success [::evt/save-cover cover]}])
    
    (dispatch [::evt/save-cover cover])))



(defn get-cover [id]
  (dispatch [::evt/get-cover id]))



(defn image-picker-button []
  [:span
   [:a {:on-click #(.click (sel1 :#image-input))}
    "image"]
   [:input#image-input
    {:type "file"
     :accept "image/*"
     :style {:display :none
             :position :inline-block}
     :on-change #(evt/set-image-url
                  (.createObjectURL js/URL (-> % .-target .-files (aget 0))))}]])


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
       [image-picker-button]
      
       (when @authenticated?
         [:a {:on-click #(save-cover @(subscribe [::sub/cover]))}
          "save"])
      
       [:a {:on-click #(do (evt-index/set-page :index)
                           (evt-index/refresh))}
        "close"])]

     (condp = @dimmer
       :font-picker [cc/font-picker]
        
       [editor-img])

     [cc/color-picker]]))

