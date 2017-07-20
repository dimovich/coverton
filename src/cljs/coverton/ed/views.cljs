(ns coverton.ed.views
  (:require [reagent.core  :as r]
            [dommy.core    :as d  :refer-macros [sel1]]
            [re-frame.core :as rf :refer [subscribe dispatch dispatch-sync]]
            [coverton.components  :as cc]
            [coverton.ed.events   :as evt]
            [coverton.ed.subs     :as sub]
            [ajax.core            :as ajax :refer [GET]]
            [coverton.util        :refer [info]]))

(enable-console-print!)

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



;; elements with "id" will set their position (by themselves!)
;;
(defn mark [id]
  (let [text  (subscribe [::sub/mark-text        id])
        pos   (subscribe [::sub/mark-pos         id])
        font  (subscribe [::sub/mark-font-family id])
        size  (subscribe [::sub/mark-font-size   id])
        state (r/atom nil)]

    (r/create-class
     {:display-name "mark"
      :component-did-mount
      (fn [this]
        (let [el     (sel1 (str "#" id))
              pivot  (sel1 ".editor-img")
              abspos (absolute-xy pivot @pos)
              _      (println "pivot" pivot)]
          (reset! state {:el    el
                         :pivot pivot
                         :pos   abspos})))
      :reagent-render
      (fn []
        [cc/draggable {:update-fn #(evt/update-pos id (relative-xy (:pivot @state)
                                                                   (:el    @state)))}
         
         [cc/toolbox {:id id}]
         
         [cc/resizable {:font-size  @size
                        :update-fn  #(dispatch [::evt/update-mark id [:font-size] %])}
          
          [cc/autosize-input {:id          id
                              :key         :input
                              :text        @text
                              :font-family @font
                              :pos         abspos
                              :update-fn   #(dispatch [::evt/update-mark id [:text] %])}]]])})))



;; get access to .editor-img by moving this function editor
(defn marks [ids]
  (into [:div]
        (for [id ids]
          ^{:key id}
          [mark id])))




(defn editor []
  (let [_    (dispatch-sync [::evt/initialize])
        dim  (subscribe [::sub/dim])
        mrks (subscribe [::sub/marks])
        ids  (subscribe [::sub/mark-ids])
        image-url (subscribe [::sub/image-url])]
    (r/create-class
     {:display-name "ed"
      :component-did-mount
      (fn [this]
        (let [img (sel1 :.editor-img)]
          ;; save image size
          (evt/update-size [(d/px img :height)
                            (d/px img :height)])))
      :reagent-render
      (fn []
        [:div.editor {:on-blur evt/handle-remove-mark}

         [:img.editor-img {:src @image-url
                           :on-click evt/handle-add-mark}]

         (condp = @dim
           :show-font-picker [cc/font-picker @mrks]
           [marks @ids])])})))

