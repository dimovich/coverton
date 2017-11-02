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



(defn add-mark [canvas [x y]]
  (info "adding mark" x y)
  (->> {:left x :top y}
       (merge defaults/mark)
       clj->js
       (IText. "")
       (#(do
           (doto canvas
             (.add %)
             (.setActiveObject %))
           (.enterEditing %)))))



(defn on-click-add-mark [canvas evt]
  (as-> (.. evt -e) $
    (.getPointer canvas $)
    (js->clj $)
    (map $ ["x" "y"])
    (add-mark canvas $)))



(defn fabric->cover [canvas]
  (info "saving cover...")
  (dispatch [::evt/fabric->cover canvas]))




(defn set-background [canvas url]
  (info "setting background: " url)
  (fromURL url
           (fn [img]
             (.scaleToWidth
              img (/  (.. canvas getWidth)
                      (.. canvas getZoom)))
             (.setBackgroundImage
              canvas img
              #(do (.renderAll canvas)
                   (fabric->cover canvas))))))




(defn cover->fabric [canvas cover]
  (.clear canvas)
  (let [fabric (:cover/fabric cover)
        url    (:cover/background cover)]
    (if-let [json (clj->js (:json fabric))]
      (do
        (info "loading from json..." json (:size fabric))
        (.loadFromJSON canvas json #(.renderAll canvas)))
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
            {"object:modified"     #(fabric->cover canvas)
             "text:editing:exited" #(handle-text-edit-exit canvas %)
             "selection:created"   #(reset! selecting? true)
             "mouse:down"          #(reset! selecting? false)
             ;;"object:added"    #(fabric->cover canvas)
             ;;"object:removed"  #(fabric->cover canvas)
             "mouse:up"
             (fn [evt]
               ;; clicking some empty space
               (when-not (or @selecting? (.. evt -target))
                 (on-click-add-mark canvas evt)))})))))





(defn handle-keys [canvas e]
  (condp = (.. e -keyCode)
    ;;Delete
    46  (some->> canvas
                 .getActiveObject
                 (.remove canvas)
                 fabric->cover)

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



(defn toolbar []
  (r/with-let
    [authenticated? (subscribe [::index-sub/authenticated?])
     uploading? (subscribe [::ed-sub/keys [:uploading?]])

     items (fn []
             [[[:img.clickable {:src "assets/svg/toolbar-background.svg"}]
               [cc/image-picker
                {:callback
                 (fn [file url]
                   (ed-evt/add-files-to-upload :cover/background file)
                   (ed-evt/set-background url))}]]
 
              [[:img.clickable {:src "assets/svg/toolbar-text.svg"}]]

              [{:style {:border 0
                        :padding 1
                        :height :auto
                        :margin "-10px 0 0 0"}}
               [:img {:src "assets/svg/h-separator.svg"}]]

              [[:img.clickable {:src "assets/svg/toolbar-preview.svg"}]]

              (when @authenticated?
                [(if @uploading?
                   {:style {:opacity 0.2}}
                   {:on-click #(cover->db)})
                 [:img.clickable {:src "assets/svg/toolbar-save.svg"}]])])]

    
    (into
     [:div.ed-toolbar]
     (map-indexed
      (fn [idx item]
        (into ^{:key idx} [toolbar-item] item))
      (filter identity (items))))))



(defn toolbar-settings []
  (r/with-let [tool  (subscribe [::ed-sub/keys [:tool]])
               items {:text [[:a {:on-click identity} "font"]
                             [:a {:on-click identity} "bold"]]}]
    (into
     [:div.ed-toolbar-settings
      (for [it (get items @tool)]
        ^{:key it}
        [:div.ed-toolbar-settings-item it])])))




(defn fabric [{url :cover/background}]
  (let [canvas     (atom nil)
        dom        (atom nil)
        parent-dom (atom nil)
        unmount-fn (atom nil)
        cover      (subscribe [::ed-sub/cover])]
    
    (r/create-class
     {:component-did-mount
      (fn [_]
        (reset! canvas (Canvas. @dom))
        (->> (init-fabric @canvas @parent-dom @cover)
             (reset! unmount-fn)))

      :component-did-update
      (fn [this]
        (info "updating fabric...")
        (->> (:cover/background (r/props this))
             (set-background @canvas)))
      
      :component-will-unmount
      (fn [this]
        (info "unmounting fabric...")
        (when @unmount-fn
          (@unmount-fn)))

      :reagent-render
      (fn [opts]
        [:div.fabric-wrap
         [toolbar]
         [toolbar-settings]
         [:div.editor-img {:ref #(reset! parent-dom %)}
          [:canvas#canv {:ref #(reset! dom %)}]]])})))




(defn editor []
  (r/with-let [
               url     (subscribe [::ed-sub/background])]
   
    [:div.editor
     [fabric {:cover/background @url}]

     #_([:br]
        [:div (str @cover)])]))

