(ns coverton.ed.views
  (:require [reagent.core  :as r]
            [dommy.core    :as d  :refer-macros [sel1]]
            [re-frame.core :as rf :refer [subscribe dispatch dispatch-sync]]
            [coverton.components  :as cc]
            [coverton.ed.events   :as evt]
            [coverton.ed.subs     :as sub]
            [ajax.core            :as ajax :refer [GET]]
            [coverton.util        :refer [info]]))


(defn relative-xy [pivot el]
  (let [rect1 (.. pivot getBoundingClientRect)
        rect2 (.. el    getBoundingClientRect)
        x (- (.. rect2 -left)
             (.. rect1 -left))
        y (- (.. rect2 -top)
             (.. rect1 -top))
        height (.. rect1 -height)
        width  (.. rect1 -width)
        w     (/ x width)
        h     (/ y height)]
    [w h]))



(defn mark [{:keys [id]}]
  (r/with-let [text  (subscribe [::sub/mark-text        id])
               pos   (subscribe [::sub/mark-pos         id])
               font  (subscribe [::sub/mark-font-family id])
               font-size  (subscribe [::sub/mark-font-size   id])
               ;;initial click coords
               [x y] @pos]

    [:div.mark {:style {:left x :top y}}
     [cc/draggable {:update-fn #(evt/update-pos id %)
                    ;;we get deltas, so we need the initial coords
                    :start-pos [x y]}
      
      [cc/toolbox {:id id}]
      
      [cc/resizable {:font-size  @font-size
                     :update-fn  #(evt/update-font-size id %)}
       
       [cc/autosize-input {:id          id
                           :key         :input
                           :text        @text
                           :font-family @font
                           :update-fn   #(evt/update-text id %)}]]]]))




(defn on-click-add-mark [parent e]
  (let [[w h] @(subscribe [::sub/size])
        rect (.. parent getBoundingClientRect)
        rx   (.. rect -left)
        ry   (.. rect -top)
        x    (- (.. e -clientX) rx)
        y    (- (.. e -clientY) ry)
        ;;h    (.. rect -height)
        ;;w    (.. rect -height)
        ]
    (evt/handle-add-mark [(/ x h) (/ y h)])))



(defn editor-img []
  (let [this      (r/current-component)
        image-url (subscribe [::sub/image-url])
        ids       (subscribe [::sub/mark-ids])]

    (r/create-class
     {:display-name "editor-img"

      :component-did-mount
      (fn [this]
        ;;set size
        ;;fixme: why height is +5 px?
        ;;todo: set size based on window size
        (let [w (d/px (r/dom-node this) :width)
              h (d/px (r/dom-node this) :height)]
          (evt/update-size [h h])))

      :reagent-render
      (fn []
        (into
         [:div.editor-img-wrap {:on-blur evt/handle-remove-mark}
          
          [:img.editor-img {:on-click #(on-click-add-mark (r/dom-node this) %)
                            :src @image-url}]]

         ;; marks
         (for [id @ids]
           ^{:key id} [mark {:id id}])))})))




(defn editor []
  (r/with-let [_    (dispatch-sync [::evt/initialize])
               dim  (subscribe [::sub/dim])
               mrks (subscribe [::sub/marks])
               ids  (subscribe [::sub/mark-ids])]

    [:div.editor 

     (condp = @dim
       :show-font-picker [cc/font-picker @mrks]
       
       [editor-img])]))

