(ns coverton.fabric.views
  (:require [reagent.core       :as r]
            [dommy.core         :as d]
            [re-frame.core      :as rf :refer [reg-event-fx dispatch]]
            [coverton.ed.subs   :as ed-sub]
            [coverton.ed.events :as ed-evt :refer [cover-interceptors]]
            [coverton.index.subs :as index-sub]
            [re-frame.core      :refer [subscribe dispatch]]
            [taoensso.timbre    :refer-macros [info]]
            [coverton.fonts     :refer [default-font]]
            [coverton.components :as cc]
            [coverton.util      :as util]
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



(defn set-canvas-size [canvas parent]
  (let [h (.. parent -clientHeight)]
    (info "setting size" h h)
    (doto canvas
      (.setWidth h)
      (.setHeight h))))



(defn click->relative [e]
  (let [bounds (.. e -target getBoundingClientRect)
        x (- (.. e -clientX) (.. bounds -left))
        y (- (.. e -clientY) (.. bounds -top))]
    [x y]))



(defn attach-text-events [text]
  (doto text
    (.on (clj->js
          {"editing:exited" identity}))))


(defn add-mark [canvas [x y]]
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
       click->relative
       (add-mark canvas)))



(reg-event-fx
 ::fabric->cover
 cover-interceptors
 (fn [{db :db} [canvas]]
   {:db (merge db {:cover/fabric
                   {:size [(.getWidth canvas) (.getHeight canvas)]
                    :json (js->clj (.toJSON canvas))
                    :svg  (.toSVG canvas (clj->js {:width "100%"
                                                   :height "100%"}))}})}))


(defn fabric->cover [canvas]
  (info "saving...")
  (dispatch [::fabric->cover canvas]))



(defn set-background [canvas url & [cb]]
  (info "setting background: " url)
  (fromURL url
           (fn [img]
             (.scaleToWidth img (.. canvas -width))
             (.setBackgroundImage canvas img
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
            {"object:modified" #(fabric->cover canvas)
             ;;"object:added"    save
             ;;"object:removed"  #(fabric->cover canvas)
             "text:editing:exited" #(handle-text-edit-exit canvas %)
             "selection:created" #(reset! selecting? true)
             "mouse:down" #(reset! selecting? false)
             "mouse:up"
             (fn [evt]
               ;; when clicking some empty space
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
  [canvas parent-dom]
    
  (set-canvas-size canvas parent-dom)
  
  (attach-events canvas)
  (d/listen! (d/sel1 :body) :keydown #(handle-keys canvas %)))




(defn unmount-fabric [canvas]
  (some-> canvas .dispose)
  (d/unlisten! (d/sel1 :body) :keydown handle-keys))




(defn fabric [{url :cover/background}]
  (let [canvas     (atom nil)
        canvas-dom (atom nil)
        parent-dom (atom nil)
        cover      (subscribe [::ed-sub/cover])]
    
    (r/create-class
     {:component-did-mount
      (fn [_]
        (reset! canvas (Canvas. @canvas-dom))
        
        (doto @canvas
          (init-fabric   @parent-dom)
          (cover->fabric @cover)))

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
      (fn []
        [:div.fabric-wrap
         [:div.editor-img {:ref #(reset! parent-dom %)}
          [:canvas#canv {:ref #(reset! canvas-dom %)}]]])})))




(defn editor []
  (r/with-let [cover (subscribe [::ed-sub/cover])
               auth? (subscribe [::index-sub/authenticated?])
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
;; scale
;; picker-block
;; controls
