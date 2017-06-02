(ns coverton.components
  (:require [reagent.core :as r]
            [re-frame.core :as rf :refer [dispatch subscribe]]
            [dommy.core :as d :refer-macros [sel1 sel]]
            [coverton.fonts :refer [default-font]]
            [coverton.editor.subs]))


(enable-console-print!)

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


;; todo: decouple re-frame logic from element
;;       pass an update-fn and a ref-fn
(defn autosize-input [{:keys [id]}]
  (let [this  (r/current-component)
        state (r/atom "")
        item  (subscribe [:item id])
        font  (subscribe [:font id])
        update-size #(set-width (r/dom-node %) @state)]
    
    (r/create-class
     {:display-name "autosize-input"
      
      :component-did-mount 
      (fn [this]
        (update-size this)
        (dispatch [:update-item id [:dom] (r/dom-node this)]))
      
      :component-did-update update-size
      
      :reagent-render
      (fn []
        (let [{:keys [font-family font-size]} @font]
          [:input {:value @state
                   :on-change #(reset! state (.. % -target -value))
                   :on-blur #(dispatch [:update-item id [:text] @state])
                   :on-key-down (fn [e] (condp = (.. e -keyCode)
                                          46 (do (reset! state "")) ;; delete
                                          27 (.. e -target blur)
                                          false))
                   :class "label-input cancel-drag"
                   :style {:font-family font-family :font-size font-size
                           :position :relative}
                   :id id
                   :auto-focus true
                   }]))})))




(defn resizable [{:keys [id]}]
  (let [state (atom nil)
        item (subscribe [:item id])
        font (subscribe [:font id])]

    (r/create-class
     {:display-namae "resizable"
      :reagent-render
      (fn [{:keys [id]}]
        (into
         ^{:key (gensym)}
         [react-resize {:class-name "label-resize"
                        :width "1em" :height "1em"
                        :lock-aspect-ratio true
                        :on-resize-start (fn [_ _ el _]
                                           (reset! state (:font-size @font))
                                           (d/add-class! el :cancel-drag))
                    
                        :on-resize-stop (fn [_ _ el d]
                                          (d/remove-class! el :cancel-drag))
                    
                        :on-resize (fn [_ _ el d]
                                     ;; element is inline, so child will set size
                                     (d/remove-style! el :height)
                                     (d/remove-style! el :width)

                                     (dispatch [:update-item id [:font :font-size]
                                                (+ @state (get-in (vec (js->clj d)) [1 1]))]))}]

         (r/children (r/current-component))))})))




(defn draggable []
  (r/with-let [this (r/current-component)]
    ^{:key (gensym)}
    [react-drag (merge (r/props this))     
     (into [:div.handle-drag]
           (r/children this))]))






(defn picker-block
  [{:keys [labels font-family]}]
  (let [size (r/atom nil)]
    (r/create-class
     {:display-name "picker-block"
      :component-did-mount
      (fn [this]
        (let [w (d/px (r/dom-node this) :width)]
          (reset! size [w w])))
      
      :reagent-render
      (fn [{:keys [labels font-family]}]
        (let [{:keys [img labels]} labels
              img-src (:src img)]
          (into
           [:div.picker-block
            [:img.picker-img {:src img-src}]]
       
           (->> labels
                (map (fn [{:keys [pos text font id]}]
                       (let [[w h] @size
                             {:keys [font-size color]} font
                             font-size (* font-size h)
                             [x y] pos
                             x (* x w)
                             y (* y h)]

                         ^{:key id}
                         [:span.picker-label
                          {:on-click #(do (dispatch [:update-item id [:font :font-family] font-family])
                                          (dispatch [:update-item id [:static] true]))
                           :style {:font-family font-family
                                   :font-size font-size
                                   :color color
                                   :top y
                                   :left x}}
                          
                          text])))))))})))


;; export
(defn export-labels [labels]
  (->> labels
       (map (fn [[id {:keys [font static dom text]}]]
              (let [img (.. (sel1 :.editor-img) getBoundingClientRect)
                    lbl (.. dom getBoundingClientRect)
                    x   (- (.. lbl -left) (.. img -left))
                    y   (- (.. lbl -top) (.. img -top))
                    w   (.. img -width)
                    h   (.. img -height)
                    x   (/ x w)
                    y   (/ y h)
                    font  (update-in font [:font-size] / h)]

                {:pos [x y]
                 :text text
                 :id id
                 :static static
                 :font font})))
       
       (assoc-in {:img {:src "assets/img/coverton.jpg"}}
                 [:labels])))



;; TODO
;; - changed flag
;;
(defn font-picker [labels]
  (into
   [:div.picker-container]
   (let [labels (export-labels @labels)]
       (for [font-family coverton.fonts/font-names]
         [picker-block {:key font-family
                        :labels labels
                        :font-family font-family}]))))




(defn dimmer [{:keys [visible]}]
  (r/with-let [this (r/current-component)]
    (r/create-class
     {:display-name "dimmer"
      :component-did-update
      (fn [this]
        (when-let [dom (r/dom-node this)]
          (let [top (- (.. dom getBoundingClientRect -top))
                left (- (.. dom getBoundingClientRect -left))]
            (d/set-px! dom :top top)
            (d/set-px! dom :left left))))
      :reagent-render
      (fn [{:keys [visible]}]
        (when visible
          (into
           [:div#dimmer]
           (r/children this))))})))



(defn toolbox-font-picker []
  (r/with-let [visible (r/atom false)
               items (subscribe [:items-with-dom])]
    [:div.label-toolbox-item {:style {:background-color "green"}
                              :on-click #(swap! visible not)}
     [dimmer {:visible @visible}
      [font-picker items]]]))



(defn toolbox []
  [:div.label-toolbox.cancel-drag
   [toolbox-font-picker]])

