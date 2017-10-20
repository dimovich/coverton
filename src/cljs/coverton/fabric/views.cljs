(ns coverton.fabric.views
  (:require [reagent.core       :as r]
            [coverton.ed.subs   :as ed-sub]
            [coverton.ed.events :as ed-evt]
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
(def parseElements window.fabric.parseElements)


(def canvas (atom nil))


(defn click->relative [e]
  (let [bounds (.. e -target getBoundingClientRect)
        x (- (.. e -clientX) (.. bounds -left))
        y (- (.. e -clientY) (.. bounds -top))]
    [x y]
    #_[(/ x (.. bounds -width))
       (/ y (.. bounds -height))]))



(defn add-mark [canvas [x y]]
  (let [mark (IText. "hello"
                     (clj->js {:left x :top y
                               :fill (:color default-font) :cursorColor (:color default-font)
                               :fontFamily (:font-family default-font) :fontSize 40}))]
    (.add canvas mark)))



(defn on-click-add-mark [canvas evt]
  (->> (.. evt -e)
       click->relative
       (add-mark canvas)))



(defn cover->fabric [canvas cover]
  (.clear canvas)
  (if-let [fabric-json (:cover/fabric-json cover)]
    (.loadFromJSON canvas fabric-json)
    ;;create new
    (let [url (:cover/image-url cover)]
      ;;background
      (fromURL url
               (fn [img]
                 (.scaleToWidth img (.. canvas -width))
                 (.setBackgroundImage canvas img
                                      (.bind canvas.renderAll canvas)))))))




(defn init-fabric [canvas]
  (.on canvas "mouse:down"
       (fn [evt]
         ;; when clicking some empty space
         (when-not (.. evt -target)
           (on-click-add-mark canvas evt)))))




(defn fabric [cover]
  (let [_     (ed-evt/initialize cover)
        cover (subscribe [::ed-sub/cover])
        fabric->json (fn [c] (.toJSON c))
        update-size (fn [canvas parent]
                      (let [h (.. parent -clientHeight)
                            h (- h 70)]
                       (.setWidth canvas h)
                       (.setHeight canvas h)))]
    (r/create-class
     {:component-did-mount
      (fn [this]
        (let [dom (r/dom-node this)
              c (Canvas. "canv")]
          
          (reset! canvas c)
          (init-fabric   @canvas)
          (update-size   @canvas (r/dom-node this))
          (cover->fabric @canvas @cover)))
      
      :component-did-update
      (fn [this])

      :component-will-unmount
      (fn [this]
        (dispatch [::ed-evt/update-cover
                   :cover/fabric-json #(fabric->json @canvas)]))
      
      :reagent-render
      (fn []
        [:div.editor
         [:div.fabric-wrap
          [:div.editor-img
           [:canvas#canv]]]
         
         [cc/color-picker2 canvas]])})))





;; TODO:
;; on-click-add-mark
;; save state

