(ns coverton.components
  (:require [reagent.core :as r]
            [dommy.core :as d :refer-macros [sel1 sel]]
            [goog.object]
            [coverton.fonts]
            [soda-ash.core]))

(enable-console-print!)

(def react-drag   (r/adapt-react-class (goog.object/get js/window.deps "draggable")))
(def react-resize (r/adapt-react-class (goog.object/get js/window.deps "resizable")))


;;
;; calculate text width in px for font type and size
;; and change element width
;;
(defn set-width [el text]
  (let [font (d/style el :font-family)
        size (d/style el :font-size)
        span (sel1 :#span-measure)]

    ;; copy styles to span
    (d/set-style! span :font-size size)
    (d/set-style! span :font-family font)
    (d/set-html!  span "")
    (d/append!    span (d/create-text-node text))

    ;; get normal width (has issues with whitespace),
    ;; so possibly extend to scroll width
    (d/set-px! el :width (+ 2 (.. span -scrollWidth)))
    (d/set-px! el :width (.. el -scrollWidth))))



(defn autosize-input [{:keys [uuid ref]}]
  (let [this (r/current-component)
        state (r/atom "")]
    (r/create-class
     {:display-name "autosize-input"
      :component-did-mount
      (fn [this]
        (ref (r/dom-node this)) ;;love
        (set-width (r/dom-node this) @state))
      :component-did-update
      (fn [this]
        (let [dom (r/dom-node this)]
          (set-width dom @state)))
      :reagent-render
      (fn [{:keys [uuid ref]}]
        ^{:key (gensym)}
        [:input {:value @state
                 :on-change #(reset! state (.. % -target -value))
                 :on-key-down (fn [e] (condp = (.. e -keyCode)
                                        46 (do (reset! state "")) ;; delete
                                        27 (.. e -target blur)
                                        false))
                 :class "label-input cancel-drag"
                 :id uuid
                 :auto-focus true}])})))




(defn resizable [{:keys [dom]}]
  (r/with-let [this (r/current-component)
               state (atom nil)]

    (into
     ^{:key (gensym)}
     [react-resize {:class-name "label-resize"
                    :width "1em" :height "1em"
                    :lock-aspect-ratio true
                    :on-resize-start (fn [_ _ el _]
                                       ;; save current font size and add delta later
                                       (reset! state (d/px el :font-size))
                                       
                                       ;; prevent selecting text (issue with stopping)
                                       ;;(some-> @ref (d/set-attr! :disabled))
                                       
                                       ;; prevent dragging
                                       (d/add-class! el :cancel-drag))
                    
                    :on-resize-stop (fn [_ _ el _]
                                      (some-> @dom (d/remove-attr! :disabled))
                                      (d/remove-class! el :cancel-drag))
                    
                    :on-resize (fn [_ _ el d]
                                 ;; element is inline, so child will set size
                                 (d/remove-style! el :height)
                                 (d/remove-style! el :width)

                                 ;; change font size based on delta
                                 (d/set-px! el :font-size
                                            (+ @state
                                               (get-in (vec (js->clj d)) [1 1])))

                                 ;; update child width
                                 (some-> @dom (set-width (.-value @dom))))}]

     (r/children this))))




(defn draggable [{:keys [dom]}]
  (r/with-let [this (r/current-component)]
    ^{:key (gensym)}
    [react-drag (merge (r/props this)
                       #_{:on-start #(some-> @dom (d/set-attr! :disabled))
                          :on-stop #(some-> @dom (d/remove-attr! :disabled))})
     
     (into [:div.handle-drag]
           (r/children this))]))




(defn toolbox [{:keys [dom data-fn]}]
  [:div.label-toolbox.cancel-drag
   [:div.label-toolbox-item {:style {:background-color "green"}
                             :on-click #(data-fn)}]
   [:div.label-toolbox-item {:style {:background-color "orange"}
                             :on-click (fn [e]
                                         (d/set-style! @dom :color "orange"))}]])




(defn picker-block [{:keys [labels font-family update-fn]}]
  (let [size (atom nil)
        update-size (fn [this]
                      (let [w (.. (sel1 :.picker-img)
                                  getBoundingClientRect
                                  -width)]
                        (reset! size [w w])
                        (d/set-px! (r/dom-node this) :height w)))]
    (r/create-class
     {:display-name "picker-block"
      :component-did-mount update-size
      :component-will-update update-size
      :reagent-render
      (fn [{:keys [labels font-family update-fn]}]
        (let [{:keys [img labels]} labels
              img-src (:src img)]
          (into
           ^{:key (gensym)}
           [:div.picker-block
            [:img.picker-img {:src img-src}]]
           (->> labels
                (map (fn [{:keys [pos text font dom uuid]}]
                       (let [[w h] @size
                             {:keys [font-size color]} font
                             font-size (* font-size h)
                             [x y] pos
                             x (* x w)
                             y (* y h)]

                         ^{:key (str x y)}
                         [:span.picker-label
                          { ;; change font of the label
                           :on-click #(do (d/set-style! dom :font-family font-family)
                                          (update-fn uuid))
                           :style {:font-family font-family
                                   :font-size font-size
                                   :color color
                                   :top y
                                   :left x}}
                          text])))))))})))



;; export
(defn export-labels [labels]
  (->> labels
       (map (fn [[uuid {:keys [dom static]}]]
              (let [img (.. (sel1 :.editor-img) getBoundingClientRect)
                    lbl (.. @dom getBoundingClientRect)
                    x   (- (.. lbl -left) (.. img -left))
                    y   (- (.. lbl -top) (.. img -top))
                    w   (.. img -width)
                    h   (.. img -height)
                    x   (/ x w)
                    y   (/ y h)
                    fs  (/ (d/px @dom :font-size) h)]
                
                {:pos [x y]
                 :text (.. @dom -value)
                 :dom @dom
                 :uuid uuid
                 :static static
                 :font {:font-family (d/style @dom :font-family)
                        :font-size fs
                        :color (d/style @dom :color)}})))
       doall
       (assoc-in {:img {:src "assets/img/coverton.jpg"}}
                  [:labels])))


;; TODO
;; - changed flag
;;
(defn font-picker [label-data]
  (let [{:keys [labels parent]} @label-data
        labels (export-labels @labels)
        update-fn #(r/force-update parent)]
    (into
     ^{:key (gensym)}
     [:div.picker-container]
     (for [font-family coverton.fonts/font-names]
       [picker-block {:labels labels
                      :font-family font-family
                      :update-fn update-fn}]))))



