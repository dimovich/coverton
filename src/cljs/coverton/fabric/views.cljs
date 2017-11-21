(ns coverton.fabric.views
  (:require [reagent.core       :as r]
            [dommy.core         :as d]
            [re-frame.core      :refer [dispatch dispatch-sync subscribe]]
            [taoensso.timbre    :refer [info]]
            [coverton.fabric.events :as evt]
            [coverton.ed.events  :as ed-evt :refer [cover-interceptors]]
            [coverton.ed.subs    :as ed-sub]
            [coverton.index.subs :as index-sub]
            [coverton.fabric.defaults :as defaults]
            [coverton.components :as cc]
            ;;["fabric"]
            [cljsjs.fabric]))

(comment
  ;; TODO
  ;; undo/redo -> try re-frame undo/redo (maybe full db is better logic?)
  ;;              disable fabric rendering on object add
)




(def Canvas  window.fabric.Canvas)
(def IText   window.fabric.IText)
(def fromURL window.fabric.Image.fromURL)


(defn dom-size [dom]
  [(.. dom -clientWidth)
   (.. dom -clientHeight)])



(defn handle-resize [canvas newsize oldsize]
  (let [[nx ny] newsize
        [ox oy] oldsize
        scale   (/ ny oy)]
    (info "resize" nx ny scale)
    (doto canvas
      (.setWidth ny)
      (.setHeight ny)
      (.setZoom scale))))



(defn add-mark [canvas]
  (info "adding mark...")
  (->> defaults/mark
       (IText. "edit me")
       (#(do
           (doto canvas
             (.add %)
             (.centerObject %)
             (.setActiveObject %))
           (doto %
             ;;(.selectAll)
             (.enterEditing))))))



(defn fabric->cover []
  (info "saving cover...")
  (dispatch [::evt/fabric->cover]))



(defn set-background [canvas url]
  (info "setting background: " url)
  (fromURL url
           (fn [img]
             (dispatch [::ed-evt/update :images
                        assoc :cover/background img])
             
             (.scaleToWidth
              img (/  (.. canvas getWidth)
                      (.. canvas getZoom)))
             (.setBackgroundImage
              canvas img
              #(do (.renderAll canvas)
                   (fabric->cover))))))




(defn cover->fabric [canvas cover]
  (.clear canvas)
  (let [fabric (:cover/fabric cover)
        url    (:cover/background cover)]
    (if-let [json (clj->js (:json fabric))]
      (do
        (info "loading from json..." json (:size fabric))
        (.loadFromJSON canvas json #(do (.renderAll canvas)
                                        (fabric->cover))))
      (do
        (info "creating new canvas...")
        (set-background canvas url)))))




(defn cover->db []
  (dispatch [::evt/cover->db]))



(defn handle-text-edit-exit
  [canvas e]
  (let [target (.. e -target)
        text (.. target -text)]

    (when (empty? text)
      (.remove canvas target))))



(defn attach-events [canvas]
  (let [selecting? (atom false)]
    (doto canvas
      (.on (clj->js
            {"object:modified"     #(fabric->cover)
             "text:editing:exited" #(handle-text-edit-exit canvas %)
             "selection:created"   #(reset! selecting? true)
             "mouse:down"          #(reset! selecting? false)
             ;;"object:added"    #(fabric->cover canvas)
             ;;"object:removed"  #(fabric->cover canvas)
             "mouse:up"
             (fn [evt]
               ;; clicking some empty space
               (when-not (or @selecting? (.. evt -target))
                 #_(on-click-add-mark canvas evt)))})))))





(defn handle-keys [canvas e]
  (condp = (.. e -keyCode)
    ;;Delete
    46  (do
          (some->> canvas
                   .getActiveObject
                   (.remove canvas))
          (fabric->cover))

    ;;Esc
    27  (some->> canvas
                 .deactivateAll
                 .renderAll)
    false))




(defn init-fabric
  [canvas dom cover]

  (let [newsize  (dom-size dom)
        origsize (:size (:cover/fabric cover))
        origsize (or origsize newsize)
        
        keydown #(handle-keys canvas %)
        resize  #(doto canvas
                   (handle-resize (dom-size dom) origsize)
                   .renderAll)
        
        evts [[(d/sel1 :body) :keydown keydown]
              [js/window      :resize  resize]]]


    (set! window.fabric.Object.NUM_FRACTION_DIGITS 10)
    
    (doto canvas
      (handle-resize newsize origsize)
      (cover->fabric cover)
      attach-events)

    
    ;; Events
    (doseq [e evts] (apply d/listen! e))

    ;; return an unmount function
    (fn []
      (info "cleaning up...")
      (doseq [e evts] (apply d/unlisten! e))
      (some-> canvas .dispose))))




(defn toolbar-item []
  (r/with-let [this (r/current-component)]
    (into
     [:div.ed-toolbar-item
      (r/props this)]
     (r/children this))))



(defn toolbar-left []
  (r/with-let
    [authenticated? (subscribe [::index-sub/authenticated?])
     uploading? (subscribe [::ed-sub/keys [:uploading?]])
     canvas (subscribe [::ed-sub/keys [:fabric :canvas]])

     items (fn []
             [[{:style {:width "85%"}
                :on-click #(add-mark @canvas)}
               [:img.clickable {:src "assets/svg/ed/text.svg"}]]
              
              [[:img.clickable {:src "assets/svg/ed/image.svg"}]
               [cc/image-picker
                {:callback
                 (fn [file url]
                   (ed-evt/add-files-to-upload :cover/background file)
                   (set-background @canvas url))}]]
 
              [{:style {:opacity 1}}
               [:img {:src "assets/svg/ed/separator.svg"}]]

              [[:img.clickable {:src "assets/svg/ed/preview.svg"}]]

              [{:style {:opacity 1
                        :margin-top "-10px"}}
               [:img {:src "assets/svg/ed/separator.svg"}]]

              (when @authenticated?
                [(if @uploading?
                   {:style {:opacity 0.3}}
                   {:on-click #(cover->db)})
                 [:img.clickable {:src "assets/svg/ed/save.svg"}]])

              [{:on-click #(ed-evt/initialize)}
               [:img.clickable {:src "assets/svg/ed/new.svg"}]]])]

    
    (into
     [:div.ed-toolbar]
     (map-indexed
      (fn [idx item]
        (into ^{:key idx} [toolbar-item] item))
      (filter identity (items))))))





(defn toolbar-top []
  (r/with-let [snapshots (subscribe [::ed-sub/keys [:fabric :snapshots]])
               snapshot-idx (subscribe [::ed-sub/keys [:fabric :snapshot-idx]])
               items (fn []
                       [[(when (some-> @snapshot-idx
                                       (< (dec (count @snapshots))))
                           {:on-click #(dispatch [::evt/undo])
                            :class :clickable
                            :style {:opacity 1}})
                         [:img {:src "assets/svg/ed/undo.svg"}]]
                        [[:img {:src "assets/svg/ed/redo.svg"}]]])]
    (into
     [:div.ed-toolbar-settings]
     (for [it (items)]
       (into ^{:key it} [:div.ed-toolbar-settings-item] it)))))








;;todo: retract tags from db cover
(defn tags [tgs]
  (r/with-let [text           (r/atom nil)
               input-visible? (r/atom false)
               set-visible (fn [b]
                             (reset! input-visible? b)
                             (reset! text nil))

               tag (fn [opts tg]
                     [:div.ed-tag
                      [:span.helper-valign]
                      [:span.clickable opts (str "#" tg)]])

               add-tag (fn [_]
                         (if (empty? @text)
                           (set-visible false)
                           (do
                             (dispatch [::ed-evt/update-cover :cover/tags
                                        #(-> (or %1 [])
                                             (conj %2))
                                        @text])
                             (reset! text nil))))
               
               remove-tag (fn [idx]
                            (dispatch [::ed-evt/update-cover :cover/tags
                                       #(vec (concat
                                              (subvec % 0 idx)
                                              (subvec % (inc idx))))]))]

    (into
     [:div.ed-tags

      ;; Tags
      (map-indexed
       (fn [idx tg]
         [tag {:on-click #(remove-tag idx)
               :key idx}
          tg])
       tgs)]
     
     (if @input-visible?
       ;; Tag Input
       [[:span {:style {:margin-top :auto
                        :margin-bottom :auto
                        :margin-right "0.1em"}} "#"]
        [cc/editable :input {:state text
                             :class :ed-tag-input
                             :auto-focus true
                             :on-blur #(set-visible false)
                             :on-key-down (fn [e]
                                            (condp = (.. e -key)
                                              "Escape"    (set-visible false)
                                              "Enter"     (add-tag)
                                              false))}]]

       ;; + button
       [[:img.clickable.ed-tag-plus
         {:src "assets/svg/ed/plus.svg"
          :on-click #(set-visible true)}]]))))













(defn fabric [cover]
  (let [dom        (atom nil)
        parent-dom (atom nil)
        unmount-fn (atom nil)]
    
    (r/create-class
     {:component-did-mount
      (fn [_]
        (let [canvas (Canvas. @dom)]
          (dispatch [::ed-evt/update :fabric assoc :canvas canvas])
          (->> (init-fabric canvas @parent-dom cover)
               (reset! unmount-fn))))

      :component-did-update
      (fn [this]
        (info "updating fabric...")
        #_(cover->fabric @canvas (r/props this))
        #_(->> (:cover/background (r/props this))
               (set-background @canvas)))
      
      :component-will-unmount
      (fn [this]
        (info "unmounting fabric...")
        (dispatch [::ed-evt/update :fabric (fn [_] nil)])
        (when @unmount-fn
          (@unmount-fn)))

      :reagent-render
      (fn [cover]
        [:div.fabric-wrap
         [toolbar-left]
         [toolbar-top]
         [:div.editor-img {:ref #(reset! parent-dom %)}
          [:canvas#canv {:ref #(reset! dom %)}]]
         [tags (:cover/tags cover)]])})))




(defn editor []
  (r/with-let [url (subscribe [::ed-sub/background])
               cover (subscribe [::ed-sub/cover])]
   
    [:div.editor
     [fabric @cover]]))

