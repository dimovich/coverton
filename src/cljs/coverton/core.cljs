(ns coverton.core
  (:require [reagent.core :as r]
            [dommy.core :as d :refer-macros [sel1]]))


(enable-console-print!)


(def react-drag (r/adapt-react-class (aget js/window "deps" "draggable")))
(def react-resize (r/adapt-react-class (aget js/window "deps" "resizable")))


(defn set-width [el text]
  (let [font (d/style el :font-family)
        size (d/style el :font-size)
        span (sel1 :#span-measure)]

    (d/set-style! span :font-size size)
    (d/set-style! span :font-family font)
    (d/set-html!  span text)

    (d/set-px! el :width (+ 2 (.. span -scrollWidth)))
    (d/set-px! el :width (.. el -scrollWidth))))



(defn autosize-input [{:keys [uuid ref]}]
  (r/with-let [state (r/atom nil)]

    (r/create-class
     {:display-name "autosize-input"
      :component-did-mount
      (fn [this]
        (let [dom (r/dom-node this)]
          (ref dom)
          (set-width dom @state)))
      :component-did-update
      (fn [this]
        (let [dom (r/dom-node this)]
          (set-width dom @state)))
      :reagent-render
      (fn []
        [:input {:value @state
                 :on-change (fn [e]
                              (let [el (.. e -target)]
                                (reset! state (.. el -value))))
                 :class "label-input cancel-drag"
                 :id uuid
                 :auto-focus true}])})))



;;
;; rewrite input using on-change, width
;;
(defn resizable [{:keys [ref]}]
  (r/with-let [this (r/current-component)
               ref-child (atom nil)
               state (atom nil)]

    (r/create-class
     {:display-name "resizable"
      :component-did-mount
      (fn [this])
      :reagent-render
      (fn []
        (into
         [react-resize {:class-name :label-resize
                        :width "1em" :height "1em"
                        :lock-aspect-ratio true
                        :on-resize-start (fn [_ _ el _]
                                           (reset! state (d/px el :font-size))
                                           (d/set-attr! @ref-child :disabled)
                                           (d/add-class! el :cancel-drag))
                        :on-resize-stop (fn [_ _ el _]
                                          (d/remove-attr! @ref-child :disabled)
                                          (d/remove-class! el :cancel-drag))
                        :on-resize (fn [_ _ el d]
                                     ;; element is inline, so child will set size
                                     (d/remove-style! el :height)
                                     (d/remove-style! el :width)
                                     
                                     (d/set-px! el :font-size
                                                (+ @state
                                                   (get-in (vec (js->clj d)) [1 1])))

                                     ;; update child
                                     (set-width @ref-child (.-value @ref-child)))}]

         (-> (r/children this)
             (update-in [0 1] assoc :ref #(do (ref %)
                                              (reset! ref-child %))))))})))


(defn draggable []
  (r/with-let [this (r/current-component)
               child-ref (atom nil)]
    [react-drag (merge (r/props this)
                       {:on-start #(d/set-attr! @child-ref :disabled)
                        :on-stop #(d/remove-attr! @child-ref :disabled)})
     (into [:div.handle-drag]
           (-> (r/children this)
               (update-in [0 1] assoc :ref #(reset! child-ref %))))]))


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
                             {:x (- (- (.. e -clientX) px) 10)
                              :y (- (- (.. e -clientY) py) 10)
                              :uuid (random-uuid)})))}]]
     (->> @labels
          (map (fn [l]
                 ^{:key (:uuid l)}
                 [:div.label-container {:style {:left (:x l) :top (:y l)}}
                  [draggable {:cancel ".cancel-drag"}
                   [resizable {}
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
