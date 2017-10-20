(ns coverton.fabric.views
  (:require [reagent.core       :as r]
            [coverton.ed.subs   :as ed-sub]
            [coverton.ed.events :as ed-evt]
            [re-frame.core      :refer [subscribe dispatch]]
            [taoensso.timbre    :refer-macros [info]]
            [coverton.fonts     :refer [default-font]]
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
  (if-let [fabric-json (:cover/fabric-json cover)]
    (do ;;init from object
      (info fabric-json)
      (.loadFromJSON canvas fabric-json (fn [objs] (info "loaded" objs))))
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
        fabric->json (fn [] (.toJSON @canvas))
        update-size (fn [canvas parent]
                      (.setWidth canvas (.. parent -clientHeight))
                      (.setHeight canvas (.. parent -clientHeight)))]
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
      (fn [this]
        (info "updated" (.toJSON @canvas))
        (cover->fabric @canvas @cover))

      :component-will-unmount
      (fn [this]
        (info "unmounting")
        ;;(info {:cover/fabric-json (fabric->json)})
        )
      
      :reagent-render
      (fn []
        [:div.editor-img
         [:canvas#canv]
         #_[:div (str @cover)]])})))





;; TODO:
;; on-click-add-mark
;; save state

