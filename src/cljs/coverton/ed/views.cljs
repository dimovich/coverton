(ns coverton.ed.views
  (:require [reagent.core  :as r]
            [dommy.core    :as d  :refer-macros [sel1]]
            [re-frame.core :as rf :refer [subscribe dispatch dispatch-sync]]
            [ajax.core     :as ajax :refer [POST GET]]
            [coverton.db.schema   :refer [magic-id]]
            [coverton.components  :as cc]
            [coverton.ed.events   :as evt]
            [coverton.ed.subs     :as sub]
            [coverton.ajax.events :as ajax-evt]
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
                        :start-pos [x y]
                        :ref child-ref}

          ;; fixme: move toolbox to inner
          [cc/toolbox {:id id
                       :ref child-ref}]
         
          [cc/resizable {:font-size  @font-size
                         :ref child-ref
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



(defn handle-add-mark [pos]
  (let [id (random-uuid)
        sid (str id)]
    (evt/add-mark {:pos     pos
                   :mark-id id})
    (evt/set-active-mark sid)))



(defn on-click-add-mark [parent e]
  (let [[w h] @(subscribe [::sub/size])
        rect  (.. parent getBoundingClientRect)
        rx    (.. rect -left)
        ry    (.. rect -top)
        x     (- (.. e -clientX) rx)
        y     (- (.. e -clientY) ry)]
    (handle-add-mark [(/ x w) (/ y h)])))



(defn image [{:keys [url]}]
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
  (when-let [file (form-data :#image-input)] ;;todo: check if already uploaded
    (dispatch [::evt/upload-file file
               {:on-success [::evt/set-image-url]}]))
  (dispatch [::evt/save-cover cover]))



(defn get-cover [id]
  (dispatch [::evt/get-cover id]))



(defn image-picker-button []
  [:span
   [cc/Button {:on-click #(.click (sel1 :#image-input))}
    "Select Image"]
   [:input#image-input
    {:type "file"
     :accept "image/*"
     :style {:display :none
             :position :inline-block}
     :on-change #(evt/set-image-url
                  (.createObjectURL js/URL (-> % .-target .-files (aget 0))))}]])




(defn editor [{:keys [cover]}]
  (r/with-let [_       (evt/initialize cover)
               dimmer  (subscribe [::sub/dimmer])
               ed-t    (subscribe [::sub/t])]

    ^{:key @ed-t} ;;forces re-mount when cover is re-initialized
    [:div.editor
     
     [:div.editor-toolbar-top

      [image-picker-button]

      [cc/Button {:on-click #(dispatch [::evt/upload-file
                                        (form-data :#image-input)
                                        {:on-success [::evt/set-image-url]}])}
       "Send"]
      
      [cc/Button {:on-click #(save-cover (sub/export-cover))}
       "Save"]
     
      [cc/Button {:on-click #(get-cover magic-id)}
       "Load"]

      [cc/Button {:on-click #(evt/initialize cover)}
       "Reset"]

      [cc/Button {:on-click #(evt-index/set-page :index)}
       "Close"]]


     (condp = @dimmer
       :font-picker [cc/font-picker]
       
       [editor-img])

     [cc/color-picker]]))




;;https://github.com/theophilusx/file-upload/blob/master/src/cljs/file_upload/core.cljs
