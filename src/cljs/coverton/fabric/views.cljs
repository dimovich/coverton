(ns coverton.fabric.views
  (:require ;;[react-fabricjs]
            [cljsjs.fabric]
            [reagent.core       :as r]
            [coverton.ed.subs   :as ed-sub]
            [coverton.ed.events :as ed-evt]
            [re-frame.core      :refer [subscribe]]
            [taoensso.timbre    :refer-macros [info]]))



(def Canvas  (goog.object/getValueByKeys js/window "fabric" "Canvas"))
(def Rect    (goog.object/getValueByKeys js/window "fabric" "Rect"))
(def Text    (goog.object/getValueByKeys js/window "fabric" "Text"))
(def IText   (goog.object/getValueByKeys js/window "fabric" "IText"))
(def fromURL (goog.object/getValueByKeys js/window "fabric" "Image" "fromURL"))


(def canvas (r/atom nil))


(defn click->relative [e]
  (let [bounds (.. e -target getBoundingClientRect)
        x (- (.. e -clientX) (.. bounds -left))
        y (- (.. e -clientY) (.. bounds -top))]
    [(/ x (.. bounds -width))
     (/ y (.. bounds -height))]))



(defn on-click-add-mark [evt]
  (info evt)
  (-> (.. evt -e)
      click->relative
      ed-evt/add-mark))


(defn init-fabric [canvas])


(defn marks->fabric [canvas marks]
  (doall
   (map (fn [[id mark]]
          (let [width (.. canvas -width)
                height (.. canvas -height)
                [x y] (:pos mark)
                x (* x width)
                y (* y height)
                text (or (:text mark) "hello")]
            (.add canvas (IText. text (clj->js {:left x :top y})))))
        marks)))



(defn cover->fabric [canvas cover]
  (.clear canvas)
  (let [url (:cover/image-url cover)
        marks (:cover/marks cover)]
    ;; background
    (fromURL url #(do (.on % "mouse:down" on-click-add-mark)
                      (.scaleToWidth % (.. canvas -width))
                      (.setBackgroundImage canvas %)
                      (.renderAll canvas)))
    ;; marks
    (marks->fabric canvas marks)))




(defn fabric [cover]
  (let [_ (ed-evt/initialize cover)
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
          (cover->fabric @canvas (r/props this))))
      
      :component-did-update
      (fn [this]
        (cover->fabric @canvas (r/props this)))
      
      :reagent-render
      (fn [cover]
        [:div.editor-img
         [:canvas#canv]])})))


;; TODO:
;; on-click-add-mark
;; save state
