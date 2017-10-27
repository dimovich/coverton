(ns coverton.fabric.views
  (:require [reagent.core       :as r]
            [dommy.core         :as d]
            [re-frame.core      :as rf :refer [reg-event-fx dispatch subscribe dispatch]]
            [taoensso.timbre    :refer [info]]
            [coverton.ed.subs   :as ed-sub]
            [coverton.ed.events :as ed-evt :refer [cover-interceptors]]
            [coverton.index.subs :as index-sub]
            [coverton.fonts      :refer [default-font]]
            [coverton.components :as cc]
            [coverton.util       :as util]
            [cljsjs.fabric]))



(def Canvas  window.fabric.Canvas)
(def Rect    window.fabric.Rect)
(def Text    window.fabric.Text)
(def IText   window.fabric.IText)
(def fromURL  window.fabric.Image.fromURL)
(def StaticCanvas  window.fabric.StaticCanvas)
(def loadSVGFromString window.fabric.loadSVGFromString)
(def groupSVGElements window.fabric.util.groupSVGElements)
(def enlivenObjects window.fabric.util.enlivenObjects)


(def state (atom nil))



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
  (->> {:left x :top y
        :fill (:color default-font) :cursorColor (:color default-font)
        :fontFamily (:font-family default-font) :fontSize 50}
       clj->js
       (IText. "")
       (#(do
           (doto canvas
             (.add %)
             (.setActiveObject %))
           (.enterEditing %)))))



(defn on-click-add-mark [canvas evt]
  (->> (.. evt -e)
       (.getPointer canvas)
       js->clj
       (#(map % ["x" "y"]))
       (add-mark canvas)))



(reg-event-fx
 ::fabric->cover
 cover-interceptors
 (fn [{db :db} [canvas]]
   (let [size (or (get-in db [:cover/fabric :size])
                  [(.getWidth canvas) (.getHeight canvas)])]
     {:db (merge db {:cover/fabric
                     {:size size
                      :json (js->clj (.toJSON canvas))
                      :svg  (.toSVG canvas (clj->js {:width "100%"
                                                     :height "100%"}))}})})))


(defn fabric->cover [canvas]
  (info "saving...")
  (dispatch [::fabric->cover canvas]))



(defn set-background [canvas url & [cb]]
  (info "setting background: " url)
  (fromURL url
           (fn [img]
             (.scaleToWidth
              img (/  (.. canvas getWidth)
                      (.. canvas getZoom)))
             (.setBackgroundImage
              canvas img
              #(do (.renderAll canvas)
                   (fabric->cover canvas)
                   (when cb (cb)))))))



(defn cover->fabric [canvas cover]
  (.clear canvas)
  (let [url (:cover/background cover)
        fabric (:cover/fabric cover)]
    (if-let [json (clj->js (:json fabric))]
      (do
        (info "loading from json..." (:size fabric) json)
        (.loadFromJSON canvas json #(.renderAll canvas)))
      (do
        (info "creating new canvas...")
        (set-background canvas url)))))




(reg-event-fx
 ::upload-cover
 cover-interceptors
 (fn [{db :db} _]
   (if-let [file (util/form-data :#image-input)]
     (do
       (swap! state assoc :upload-on-update? true)
       {:dispatch [::ed-evt/upload-file file
                   {:on-success [::ed-evt/merge-cover]}]})
     
     {:dispatch [::ed-evt/upload-cover]})))





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
             ;;"object:added"    save
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
    (swap! state update :run-unmount concat
           (for [e evts] #(apply d/unlisten! e)))))




(defn unmount-fabric [canvas]
  (some-> canvas .dispose)
  (doseq [f (:run-unmount @state)] (f))
  (reset! state nil))




(defn fabric [{url :cover/background}]
  (let [canvas (atom nil)
        dom    (atom nil)
        parent-dom (atom nil)
        cover  (subscribe [::ed-sub/cover])]
    
    (r/create-class
     {:component-did-mount
      (fn [_]
        (reset! canvas (Canvas. @dom))
        (init-fabric @canvas @parent-dom @cover))

      :component-did-update
      (fn [this]
        (info "updating fabric...")
        
        (set-background
         @canvas  (:cover/background (r/props this))
         
         (when (:upload-on-update? @state)
           (swap! state dissoc :upload-on-update?)
           #(dispatch [::ed-evt/upload-cover]))))
      
      :component-will-unmount
      (fn [this]
        (info "unmounting fabric...")
        (unmount-fabric @canvas))

      :reagent-render
      (fn [opts]
        [:div.fabric-wrap
         [:div.editor-img {:ref #(reset! parent-dom %)}
          [:canvas#canv {:ref #(reset! dom %)}]]])})))




(defn editor []
  (r/with-let [auth? (subscribe [::index-sub/authenticated?])
               url   (subscribe [::ed-sub/background])]
   
    [:div.editor
     [:div.header {:style {:text-align :center
                           :margin "0.5em auto"}}
      (cc/menu
       [cc/image-picker ed-evt/set-background]
       
       (when @auth?
         [:a {:on-click #(dispatch [::upload-cover])}
          "save"]))]

     [fabric {:cover/background @url}]

     #_([:br]
        [:div (str @cover)])]))



;; TODO:
;;
;; picker-block
;; controls
;;




#_(defn attach-text-events [text]
    (doto text
      (.on (clj->js
            {"editing:exited" identity}))))


;; mobile touch
;; https://developer.mozilla.org/en-US/docs/Web/API/Touch_events#Example
