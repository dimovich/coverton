(ns coverton.components
  (:require [reagent.core :as r]
            [dommy.core :as d :refer-macros [sel1]]))



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
    (d/append! span (d/create-text-node text))

    ;; get normal width (has issues with whitespace),
    ;; so possibly extend to scroll width
    (d/set-px! el :width (+ 2 (.. span -scrollWidth)))
    (d/set-px! el :width (.. el -scrollWidth))))


;;
;; ref will resize item width
;;
(defn autosize-input [{:keys [uuid ref color]}]
  (r/with-let [this (r/current-component)
               state (r/atom "")]
    (r/create-class
     {:display-name "autosize-input"
      :component-did-mount
      (fn [this]
        (ref (r/dom-node this))
        (set-width (r/dom-node this) @state))
      :component-did-update
      (fn [this]
        (let [dom (r/dom-node this)]
          (set-width dom @state)))
      :reagent-render
      (fn []
        [:input {:value @state
                 :on-change #(reset! state (.. % -target -value))
                 :on-key-up (fn [e] (condp = (.. e -keyCode)
                                      ;; 46 (reset! state "") ;; delete
                                      27 (.. e -target blur)
                                      false))
                 :class "label-input cancel-drag"
                 :style {:color color}
                 :id uuid
                 :auto-focus true}])})))




(defn resizable [{:keys [dom]}]
  (r/with-let [this (r/current-component)
               ;;ref (atom nil)
               state (atom nil)]

    (into
     [react-resize {:class-name "label-resize"
                    :width "1em" :height "1em"
                    :lock-aspect-ratio true
                    :on-resize-start (fn [_ _ el _]
                                       ;; save current font size and add delta later
                                       (reset! state (d/px el :font-size))
                                       
                                       ;; prevent selecting text
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

                                 ;; change font size
                                 (d/set-px! el :font-size
                                            (+ @state
                                               (get-in (vec (js->clj d)) [1 1])))

                                 ;; update child width
                                 (some-> @dom
                                         (set-width (.-value @dom))))}]

     (-> (r/children this)
         ;; pass-through the ref to the child
         ;;(update-in [0 1] assoc :ref #(reset! ref %))
         ))))




(defn draggable []
  (r/with-let [this (r/current-component)
               ;;ref (atom nil)
               ]
    
    [react-drag (merge (r/props this)
                       #_{:on-start #(some-> @ref (d/set-attr! :disabled))
                          :on-stop #(some-> @ref (d/remove-attr! :disabled))})
     
     (into [:div.handle-drag]
           (r/children this)
           #_(-> (r/children this)
                 (update-in [0 1] assoc :ref #(reset! ref %))))]))



(defn toolbox [{:keys [dom]}]
  [:div.label-toolbox.cancel-drag
   [:div.label-toolbox-item {:style {:background-color "green"}
                             :on-click (fn [e]
                                         (d/set-style! @dom :color "green"))}]
   [:div.label-toolbox-item {:style {:background-color "orange"}
                             :on-click (fn [e]
                                         (d/set-style! @dom :color "orange"))}]])
