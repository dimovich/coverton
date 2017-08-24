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


;; use center of element for position
;; how to recover? (in

(defn mark [{:keys [id]}]
  (let [text  (subscribe [::sub/mark-text id])
        pos   (subscribe [::sub/mark-pos  id])
        color (subscribe [::sub/color     id])
        font-family  (subscribe [::sub/mark-font-family id])
        font-size    (subscribe [::sub/mark-font-size   id])
        ref (atom nil)
        ;;initial click coords
        [x y] @pos]

    (r/create-class
     {:display-name "mark"
      :component-did-mount
      (fn [this])

      :reagent-render
      (fn []
        [:div.mark {:style {:left x :top y}}
         
         [cc/draggable {:update-fn #(evt/set-pos id %)
                        ;;we get deltas, so we need the initial coords
                        :start-pos [x y]
                        :ref ref}

          ;; fixme: move toolbox to inner
          [cc/toolbox {:id id
                       :ref ref}]
         
          [cc/resizable {:font-size  @font-size
                         :ref ref
                         :update-fn  #(evt/set-font-size id %)}
          
           [cc/autosize-input {:id          id
                               :set-ref     #(reset! ref %)
                               :key         :input
                               :text        @text
                               :color       @color
                               :font-family @font-family
                               :update-fn   #(evt/set-text id %)}]]]])})))



(defn on-click-add-mark [parent e]
  (let [[w h] @(subscribe [::sub/size])
        rect (.. parent getBoundingClientRect)
        rx   (.. rect -left)
        ry   (.. rect -top)
        x    (- (.. e -clientX) rx)
        y    (- (.. e -clientY) ry)]
    (evt/handle-add-mark [(/ x w) (/ y h)])))




;; TODO: add :on-load
(defn image [{:keys [url]}]
  (let [this        (r/current-component)
        update-size (fn [this]
                      (let [w (d/px (r/dom-node this) :width)
                            h (d/px (r/dom-node this) :height)]
                        (println w h (.. (r/dom-node this) getBoundingClientRect -width))
                        (evt/set-size [w h])))]
    (r/create-class
     {:display-name "image"
      :component-did-mount  update-size
      :component-did-update update-size
      :reagent-render
      (fn [{:keys [url]}]
        [:img.editor-img {:on-click #(on-click-add-mark (r/dom-node this) %e)
                          :src url}])})))


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
         [:div.editor-img-wrap {:on-blur evt/handle-remove-mark}

          [image {:url @image-url}]
          
          (when @size
            (for [id @ids]
              ^{:key id} [mark {:id id}]))]

         ;; marks
         ;; make sure we have the size set
         ))})))




(defn save-cover [cover]
  (POST "/save-cover" {:handler (fn [res]
                                  (evt/set-cover-id (:cover-id res))
                                  (info res))
                       :error-handler #(.log js/console (str %))
                       :params {:cover cover}}))


(defn get-cover [id]
  (POST "/get-cover" {:handler (fn [cover]
                                 (info cover)
                                 (evt/initialize cover))
                      :error-handler #(info %)
                      :params {:id id}}))



(defn image-picker []
  [:span
   [cc/Button {:on-click #(.click (sel1 :#image-input))}
    "Select Image"]
   [:input#image-input
    {:type "file"
     :accept "image/*"
     :style {:display :none}
     :on-change #(evt/set-image-url
                  (.createObjectURL js/URL (-> % .-target .-files (aget 0))))}]])




(defn editor [{:keys [cover]}]
  (r/with-let [_       (evt/initialize cover)
               dimmer  (subscribe [::sub/dimmer])
               ed-t    (subscribe [::sub/t])]

    ^{:key @ed-t} ;;forces re-mount when cover is re-initialized
    [:div.editor
     
     [:div.editor-toolbar-top

      [image-picker]
      
      [cc/Button {:on-click #(save-cover (sub/export-cover))}
       "Save"]
     
      [cc/Button {:on-click #(get-cover magic-id)}
       "Load"]

      [cc/Button {:on-click #(evt/initialize cover)}
       "Reset"]

      [cc/Button {:on-click #(evt-index/pop-panel)}
       "Close"]]


     (condp = @dimmer
       :font-picker [cc/font-picker]
       
       [editor-img])

     [cc/color-picker]]))

