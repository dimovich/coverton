(ns coverton.components
  (:require [reagent.core :as r]
            [dommy.core :as d :refer-macros [sel1]])
  (:require-macros [devcards.core :refer [defcard-rg]]))



(def react-drag   (r/adapt-react-class (aget js/window "deps" "draggable")))
(def react-resize (r/adapt-react-class (aget js/window "deps" "resizable")))


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
  (r/with-let [this (r/current-component)
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
      (fn []
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
    
    [react-drag (merge (r/props this)
                       #_{:on-start #(some-> @dom (d/set-attr! :disabled))
                          :on-stop #(some-> @dom (d/remove-attr! :disabled))})
     
     (into [:div.handle-drag]
           (r/children this))]))



(defn toolbox [{:keys [dom]}]
  [:div.label-toolbox.cancel-drag
   [:div.label-toolbox-item {:style {:background-color "green"}
                             :on-click (fn [e]
                                         (d/set-style! @dom :color "green"))}]
   [:div.label-toolbox-item {:style {:background-color "orange"}
                             :on-click (fn [e]
                                         (d/set-style! @dom :color "orange"))}]])




(defn font-picker [state]
  (let [size (atom nil)]
    (r/create-class
     {:display-name "font-picker"
      :component-did-mount
      (fn [this]
        (reset! size [(d/px (r/dom-node this) :width)
                      (d/px (r/dom-node this) :height)]))
      :reagent-render
      (fn []
        (let [{:keys [img labels]} @state
              {:keys [src]} img]
          [:div.picker-container
           (into
            [:div.picker-block
       
             [:img.picker-img {:src src}]]
      
            (->> labels
                 (map (fn [{:keys [pos text font]}]
                        (let [[w h] @size
                              {:keys [font-family font-size color]} font
                              font-size (* font-size h)
                              [x y] pos
                              x (* x w)
                              y (* y h)]
                    
                          [:div.picker-label {:style {:font-family font-family
                                                      :font-size font-size
                                                      :color color
                                                      :top y
                                                      :left x}}
                           text])))))]))})))






#_{:img {:src "assets/img/coverton.jpg" :w 480 :h 480}
   :labels [{:pos ["50%" "50%"]
             :text "Hello"
             :font {:font-family "GothaPro"
                    :font-size 30
                    :color :orange}}
            {:pos ["10%" "-5.56%"]
             :text "fsdfdff", :font {:font-family "GothaPro"
                                     :font-size "30px"
                                     :color "rgb(255, 165, 0)"}}]}


(defcard-rg label
  (fn [data-atom _]
    [draggable {:dom data-atom
                :cancel ".cancel-drag"}
     [toolbox {:dom data-atom}]
     [resizable {:dom data-atom}
      [autosize-input {:ref #(reset! data-atom %)
                       :uuid 143434}]]])
  (atom nil))
