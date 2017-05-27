(ns coverton.core
  (:require [reagent.core :as r]
            [dommy.core :as d :refer-macros [sel1]]
            [komponentit.autosize :as autosize]))


(enable-console-print!)


(def react-drag (r/adapt-react-class (aget js/window "deps" "draggable")))
(def react-resize (r/adapt-react-class (aget js/window "deps" "resizable")))



(defn autosize-input [{:keys [uuid]}]
  (r/with-let [state (r/atom nil)]

    [autosize/input {:value @state
                     :on-change (fn [e] (reset! state (.. e -target -value)))
                     :class "label-input cancel-drag"
                     :style {:font-size "1em"
                             :font-family "GothaPro"
                             :color :orange
                             :border 0
                             :position :relative
                             :background-color :transparent}
                     :id uuid
                     :auto-focus true
                     :on-resize #(println "on-resize")}]))



;;
;; rewrite input using on-change, width
;;
(defn resizable []
  (r/with-let [this (r/current-component)
               size (r/atom 60)
               handler #(do (println (d/px %3 :height))
                            (reset! size (- (d/px %3 :height) 35)))]

    (into
     [react-resize {:class-name :label-resize
                    :width "1em" :height "1em"
                    :style {:font-size @size
                            :position :absolute}
                    :lock-aspect-ratio true
                    :on-resize-start #(d/add-class! %3 :cancel-drag)
                    :on-resize-stop #(d/remove-class! %3 :cancel-drag)
                    :on-resize handler}]
     (r/children this))))


(defn draggable []
  (r/with-let [this (r/current-component)]
    [react-drag (r/props this)
     (into [:div.handle-drag]
           (r/children this))]))


(defn editor []
  (r/with-let [labels (r/atom nil)]
    (into
     [:div {:on-blur (fn [e]
                       (let [text (.. e -target -value)
                             id (uuid (.. e -target -id))]
                         (when (empty? text)
                           (swap! labels (fn [coll]
                                           (remove #(= id (:uuid %)) coll))))))}
      [:div.editor
       {:on-click (fn [e]
                    (let [rect (.. e -target -parentElement  getBoundingClientRect)
                          px (.. rect -left)
                          py (.. rect -top)]
                      (swap! labels conj
                             {:x (- (.. e -clientX) px)
                              :y (- (.. e -clientY) py)
                              :uuid (random-uuid)})))}]]

     (->> @labels
          (map (fn [l]
                 ^{:key (:uuid l)}
                 [:div.label-container {:style {:left (:x l) :top (:y l)}}
                  [draggable {:cancel ".cancel-drag"}
                   [resizable
                    [autosize-input l]]]]))
          doall))))



(defn ^:export init []
  (when js/document
    (do
      (r/render [editor] (sel1 :.app)))))



;;
;; fade-in fade-out of border
;; del on key press
;;
#_(:on-key-up (fn [e] (when (= 46 (.. e -keyCode))
                        (do
                          (reset! state nil)))))


;;[ContainerDimensions {} (fn [height] (r/as-element [my-component {:height height}]))]
