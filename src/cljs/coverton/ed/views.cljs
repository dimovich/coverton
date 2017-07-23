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


(defn absolute-xy [pivot [x y]]
  (let [rect (.. pivot getBoundingClientRect)
        top  (.. rect -top)
        left (.. rect -left)]
    [(+ x left)
     (+ y top)]))


(defn mark [{:keys [id]}]
  (let [text  (subscribe [::sub/mark-text        id])
        pos   (subscribe [::sub/mark-pos         id])
        font  (subscribe [::sub/mark-font-family id])
        size  (subscribe [::sub/mark-font-size   id])]

    (r/create-class
     {:display-name "mark"

      :component-did-mount
      (fn [this])
      
      :reagent-render
      (fn []
        (let [[x y] @pos
              _ (println "hello" x y)]
          [:div.mark {:style {:left x :top y}}
           [cc/draggable {:update-fn identity
                          #_(evt/update-pos id (relative-xy (:pivot @state)
                                                            (:el    @state)))}
         
            [cc/toolbox {:id id}]
         
            [cc/resizable {:font-size  @size
                           :update-fn  #(dispatch [::evt/update-mark id [:font-size] %])}
          
             [cc/autosize-input {:id          id
                                 :key         :input
                                 :text        @text
                                 :font-family @font
                                 :update-fn   #(dispatch [::evt/update-mark id [:text] %])}]]]]))})))




(defn on-click-add-mark [parent e]
  (let [rect (.. parent getBoundingClientRect)
        rx   (.. rect -left)
        ry   (.. rect -top)
        x    (- (.. e -clientX) rx)
        y    (- (.. e -clientY) ry)
        h    (.. rect -height)
        w    (.. rect -height)] ;; fixme: use @size instead
    (evt/handle-add-mark [(/ x h) (/ y h)])))



(defn editor-img []
  (let [this      (r/current-component)
        image-url (subscribe [::sub/image-url])
        ids       (subscribe [::sub/mark-ids])]

    (r/create-class
     {:display-name "editor-img"

      :component-did-mount
      (fn [this]
        ;; save image size
        (let [h (d/px (r/dom-node this) :height)
              w (d/px (r/dom-node this) :height)]
          (evt/update-size [w h])))

      :reagent-render
      (fn []
        (into
         [:div.editor-img-wrap {:on-click #(on-click-add-mark (r/dom-node this) %)}
          [:img.editor-img {:src @image-url}]
          #_[:div.mark {:style {:top "10%" :left "20%"
                                :width "20%" :height "20%"}}]]

         ;; marks
         (for [id @ids]
           ^{:key id} [mark {:id id}])))})))




(defn editor []
  (r/with-let [_    (dispatch-sync [::evt/initialize])
               dim  (subscribe [::sub/dim])
               mrks (subscribe [::sub/marks])
               ids  (subscribe [::sub/mark-ids])]

    [:div.editor {:on-blur evt/handle-remove-mark}

     (condp = @dim
       :show-font-picker [cc/font-picker @mrks]
       
       [editor-img])]))

