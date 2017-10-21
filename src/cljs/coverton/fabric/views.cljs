(ns coverton.fabric.views
  (:require [reagent.core       :as r]
            [coverton.ed.subs   :as ed-sub]
            [coverton.ed.events :as ed-evt]
            [coverton.index.subs :as index-sub]
            [re-frame.core      :refer [subscribe dispatch]]
            [taoensso.timbre    :refer-macros [info]]
            [coverton.fonts     :refer [default-font]]
            [coverton.components :as cc]
            [cljsjs.fabric]))



(def Canvas  window.fabric.Canvas)
(def Rect    window.fabric.Rect)
(def Text    window.fabric.Text)
(def IText   window.fabric.IText)
(def fromURL  window.fabric.Image.fromURL)
(def loadSVGFromString window.fabric.loadSVGFromString)
(def groupSVGElements window.fabric.util.groupSVGElements)
(def enlivenObjects window.fabric.util.enlivenObjects)


(def canvas (atom nil))


(defn click->relative [e]
  (let [bounds (.. e -target getBoundingClientRect)
        x (- (.. e -clientX) (.. bounds -left))
        y (- (.. e -clientY) (.. bounds -top))]
    [x y]))

#_[(/ x (.. bounds -width))
   (/ y (.. bounds -height))]




(defn add-mark [canvas [x y]]
  (let [mark (->> {:left x :top y
                   :fill (:color default-font) :cursorColor (:color default-font)
                   :fontFamily (:font-family default-font) :fontSize 40}
                  clj->js
                  (IText. "hello"))]
    (.add canvas mark)))



(defn on-click-add-mark [canvas evt]
  (->> (.. evt -e)
       click->relative
       (add-mark canvas)))



(defn fabric->cover [canvas]
  (dispatch [::ed-evt/update-cover
             :cover/fabric-json #(js->clj (.toJSON canvas))]))



(defn set-background [canvas url]
  (info "setting background: " url)
  (fromURL url
           (fn [img]
             (.scaleToWidth img (.. canvas -width))
             (.setBackgroundImage canvas img
                                  #(do (fabric->cover canvas)
                                       (.renderAll canvas))))))


(defn cover->fabric [canvas cover]
  ;;(.clear canvas)
  (let [url (:cover/image-url cover)]
    (if-let [fabric-json (clj->js (:cover/fabric-json cover))]
      (do
        (info "loading from json..." fabric-json)
        (.loadFromJSON canvas fabric-json))
      (set-background canvas url))))




(defn init-fabric [canvas]
  (let [save #(do (info "saving...")
                  (fabric->cover canvas))]
    (doto canvas
      (.on (clj->js
            {"object:modified" save
             "object:added" save
             "object:removed" save
             "mouse:down"
             (fn [evt]
               ;; when clicking some empty space
               (when-not (.. evt -target)
                 (on-click-add-mark canvas evt)))})))))




(defn set-canvas-size [canvas parent]
  (let [h (.. parent -clientHeight)]
    (.setWidth canvas h)
    (.setHeight canvas h)))



(defn fabric [cover]
  (r/create-class
   {:component-did-mount
    (fn [this]
      (let [dom (r/dom-node this)
            c (Canvas. "canv")]
        
        (reset! canvas c)
        (init-fabric     @canvas)
        (set-canvas-size @canvas (r/dom-node this))
        (cover->fabric   @canvas cover)
        (fabric->cover   @canvas)))
    
    :component-did-update
    (fn [this]
      (info "updating fabric..."))

    :component-will-unmount
    (fn [this]
      (info "unmounting fabric...")
      (fabric->cover @canvas))
    
    :reagent-render
    (fn []
      [:div.fabric-wrap
       [:div.editor-img
        [:canvas#canv]]])}))




(defn editor [{c :cover}]
  (r/with-let [_     (ed-evt/initialize c)
               cover (subscribe [::ed-sub/cover])
               authenticated? (subscribe [::index-sub/authenticated?])]
    
    [:div.editor
     [:div.header {:style {:text-align :center
                           :margin "0.5em auto"}}
      (cc/menu
       [cc/image-picker-button
        #(do (info "img-picker" %)
             (ed-evt/set-image-url %)
             (set-background @canvas %))]
       
       (when @authenticated?
         [:a {:on-click #(do (cc/save-cover @cover))}
          "save"]))]
     
     [fabric @cover]

     [:br]
     [:div (str @cover)]]))



;; TODO:
;;
;; scale
;; picker-block
;; controls
;; background
