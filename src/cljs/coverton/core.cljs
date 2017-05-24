(ns coverton.core
  (:require [reagent.core :as r]
            [dommy.core :as d :refer-macros [sel1]]
            [komponentit.autosize :as autosize]))

(enable-console-print!)

(def draggable (r/adapt-react-class (aget js/window "deps" "draggable")))
(def resizable (r/adapt-react-class (aget js/window "deps" "resizable")))


(defn get-xy [el]
  )


(defn autosize-input [{:keys [uuid]}]
  (r/with-let [state (r/atom nil)
               size (r/atom {:height 60})
               ;; FIXME: get font size from height better
               handler #(do (d/remove-style! %3 :width)
                            (swap! size assoc
                                   :height (- (d/px %3 :height)
                                              26)))]

    [draggable {:handle ".label-border"}
     [:div
      [resizable {:class-name :label-resize
                  :width "1em" :height "1em"
                  :lock-aspect-ratio true
                  :on-resize handler}
       
       [:div.label-border
        [autosize/input {:value @state
                         :on-change (fn [e] (reset! state (.. e -target -value)))
                         :class :editor-label
                         :id uuid
                         :style {:font-size (:height @size)}
                         :auto-focus true}]]]]]))

(defn editor []
  (r/with-let [labels (r/atom nil)]
    (into
     [:div.editor
      ;; delete empty labels
      {:on-blur (fn [e]
                  (let [text (.. e -target -value)
                        id (uuid (.. e -target -id))]
                    (when (empty? text)
                      (swap! labels (fn [coll]
                                      (remove #(= id (:uuid %)) coll))))))}

      [:img.editor-image {:src "assets/img/coverton.jpg"
                          :on-click (fn [e]
                                      (let [rect (.. e -target -parentElement  getBoundingClientRect)
                                            px (.. rect -left)
                                            py (.. rect -top)
                                            id (random-uuid)]
                                        (swap! labels conj
                                               {:x (- (.. e -clientX) px)
                                                :y (- (.. e -clientY) py)
                                                :uuid id})))}]]

     (->> @labels
          (map (fn [l]
                 ^{:key (:uuid l)}
                 [:div.label-container {:style {:left (:x l)
                                                :top (:y l)}}

                  [autosize-input l]]))
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
