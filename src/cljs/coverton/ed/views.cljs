(ns coverton.ed.views
  (:require [reagent.core  :as r]
            [dommy.core    :as d  :refer-macros [sel1]]
            [re-frame.core :as rf :refer [subscribe dispatch dispatch-sync]]
            [ajax.core     :as ajax :refer [POST GET]]
            [coverton.db.schema   :refer [magic-id]]
            [coverton.components  :as cc]
            [coverton.ed.events   :as evt]
            [coverton.ed.subs     :as sub]
            [coverton.util        :refer [info]]
            [coverton.index.events :as evt-index]))


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



;; use center of element for position
;; how to recover? (in

(defn mark [{:keys [id]}]
  (let [text  (subscribe [::sub/mark-text        id])
        pos   (subscribe [::sub/mark-pos         id])
        font-family  (subscribe [::sub/mark-font-family id])
        font-size    (subscribe [::sub/mark-font-size   id])
        ref (atom nil)
        ;;initial click coords
        [x y] @pos
        _ (println @pos text)]

    (r/create-class
     {:display-name "mark"
      :component-did-mount
      (fn [this]
        (let [_ (println "mark mounted")]))
      
      :reagent-render
      (fn []
        [:div.mark {:style {:left x :top y}}
         [cc/draggable {:update-fn #(evt/update-pos id %)
                        ;;we get deltas, so we need the initial coords
                        :start-pos [x y]
                        :ref ref}
      
          [cc/toolbox {:id id}]
      
          [cc/resizable {:font-size  @font-size
                         :ref ref
                         :update-fn  #(evt/update-font-size id %)}
       
           [cc/autosize-input {:id          id
                               :set-ref     #(reset! ref %)
                               :key         :input
                               :text        @text
                               :font-family @font-family
                               :update-fn   #(evt/update-text id %)}]]]])})))




(defn on-click-add-mark [parent e]
  (let [[w h] @(subscribe [::sub/size])
        rect (.. parent getBoundingClientRect)
        rx   (.. rect -left)
        ry   (.. rect -top)
        x    (- (.. e -clientX) rx)
        y    (- (.. e -clientY) ry)]
    (evt/handle-add-mark [(/ x w) (/ y h)])))



(defn editor-img []
  (let [this      (r/current-component)
        image-url (subscribe [::sub/image-url])
        ids       (subscribe [::sub/mark-ids])
        size      (subscribe [::sub/size])]

    (r/create-class
     {:display-name "editor-img"

      :component-did-mount
      (fn [this]
        ;;set size
        ;;fixme: why height is +5 px?
        ;;todo: set size based on window size
        (let [w (d/px (r/dom-node this) :width)
              h (d/px (r/dom-node this) :height)
              _ (println "editor-img mounted")]
          (evt/update-size [h h])))

      :reagent-render
      (fn []
        (into
         [:div.editor-img-wrap {:on-blur evt/handle-remove-mark}
          
          [:img.editor-img {:on-click #(on-click-add-mark (r/dom-node this) %)
                            :src @image-url}]]

         ;; marks
         ;; make sure we have the size set
         (when @size
           (for [id @ids]
             ^{:key id} [mark {:id id}]))))})))



(defn save-cover [cover]
  (POST "/save-cover" {:handler (fn [res]
                                  (evt/update-cover-id (:cover-id res))
                                  (info res))
                       :error-handler #(.log js/console (str %))
                       :params {:cover cover}}))


(defn get-cover [id]
  (POST "/get-cover" {:handler (fn [cover]
                                 (info cover)
                                 (evt/initialize cover))
                      :error-handler #(info %)
                      :params {:id id}}))




(defn editor [{:keys [cover]}]

  (r/with-let [_    (evt/initialize cover)
               dim  (subscribe [::sub/dim])]

    [:div.editor
     
     [:div.editor-toolbar-top
      [cc/Button {:on-click #(save-cover @(subscribe [::sub/cover]))}
       "Save"]
     
      [cc/Button {:on-click #(get-cover magic-id)}
       "Load"]

      [cc/Button {:on-click #(evt/initialize cover)}
       "Reset"]

      [cc/Button {:on-click #(evt-index/pop-panel)}
       "Close"]]

     (condp = @dim
       :show-font-picker [cc/font-picker]
       
       [editor-img])]))

