(ns coverton.components
  (:require [reagent.core   :as r]
            [re-frame.core  :as rf :refer [dispatch subscribe]]
            [dommy.core     :as d  :refer-macros [sel1 sel]]
            [coverton.fonts :refer [default-font]]
            [coverton.ed.events :as evt]))


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




(defn autosize-input [{:keys [id update-fn text font-family ref]}]
  (let [state   (r/atom (or text ""))
        update  #(set-width (r/dom-node %) @state)]
    
    (r/create-class
     {:display-name "autosize-input"
      
      :component-did-mount
      (fn [this]
        (ref #(update this))
        (update this))

      :component-did-update update
      
      :reagent-render
      (fn [{:keys [font-family]}]
        [:input {:value @state
                 :on-change #(reset! state (.. % -target -value))
                 :on-blur #(update-fn @state)
                 :on-key-down (fn [e] (condp = (.. e -keyCode)
                                        46 (do (reset! state "")) ;; delete
                                        27 (.. e -target blur)
                                        false))
                 :class "label-input cancel-drag"
                 :style {:font-size   "1em"
                         :font-family font-family}
                 :id id
                 :auto-focus true}])})))




(defn resizable [{:keys [font-size update-fn]}]
  (r/with-let [this  (r/current-component)
               start (atom nil)
               size  (atom nil)
               ref   (atom nil)]
    (into
     [react-resize {:class-name "label-resize"
                    :width "1em" :height "1em"
                    :style {:font-size font-size}
                    :lock-aspect-ratio true
                    :on-resize-start (fn [_ _ el _]
                                       (reset! start (d/px el :font-size))
                                       (d/add-class! el :cancel-drag))
                    
                    :on-resize-stop (fn [_ _ el d]
                                      (update-fn @size)
                                      (d/remove-class! el :cancel-drag))
                    
                    :on-resize (fn [_ _ el d]
                                 ;; element is inline, so child will set size
                                 (d/remove-style! el :height)
                                 (d/remove-style! el :width)

                                 (reset! size (+ @start
                                                 (get-in (vec (js->clj d)) [1 1])))

                                 (d/set-px! el :font-size @size)

                                 ;; run child update fn
                                 (@ref))}]
     (-> (r/children this)
         (update-in [0 1] assoc :ref #(reset! ref %))))))



(defn relative-pos [el]
  (let [er (.. el getBoundingClientRect)
        pr (.. el -parentNode getBoundingClientRect)
        x  (- (.. er -left) (.. pr -left))
        y  (- (.. er -top)  (.. pr -top))]
    [x y]))




(defn draggable [{:keys [pos update-fn]}]
  (r/with-let [this (r/current-component)
               [x y] pos];;prevents redrawing on db update
    [react-drag {:cancel ".cancel-drag"
                 :on-stop (fn [e d]
                            (let [el (aget d "node")]
                              (update-fn (relative-pos el))))}
     
     (into
      [:div.react-draggable-child {:style {:left x :top y}}]
      (r/children this))]))



(defn picker-block [{:keys [labels font-family]}]
  
  (let [size (r/atom nil)]
    (r/create-class
     {:display-name "picker-block"

      :component-did-mount
      #(let [w (d/px (sel1 :.picker-img) :width)]
         (reset! size [w w]))
      
      :reagent-render
      (fn [{:keys [labels font-family]}]
        (let [{:keys [img labels]} labels
              block-family font-family
              img-src (:src img)]
          (into
           [:div.picker-block
            [:img.picker-img {:src img-src}]]
       
           (->> labels
                (map (fn [{:keys [pos text font-size font-family color id static]}]
                       (let [[w h] @size
                             font-size (* font-size h)
                             [x y] pos
                             x (* x w)
                             y (* y h)]

                         ^{:key id}
                         [:span.picker-label
                          {:on-click #(do (dispatch [::evt/update-mark id [:font-family] block-family])
                                          (dispatch [::evt/update-mark id [:static] true]))
                           :style {:font-family (if static font-family block-family)
                                   :font-size font-size
                                   :color color
                                   :top y
                                   :left x}}
                          
                          text])))))))})))


;; export
(defn export-labels [labels]
  (let [img (.. (sel1 :.editor-img) getBoundingClientRect)]
    (assoc-in {:img {:src "assets/img/coverton.jpg"}} [:labels]
              (for [lbl (sel :.label-input)]
                (let [rect (.. lbl getBoundingClientRect)
                      x   (- (.. rect -left) (.. img -left))
                      y   (- (.. rect -top) (.. img -top))
                      w   (.. img -width)
                      h   (.. img -height)
                      x   (/ x w)
                      y   (/ y h)
                      font-family (d/style lbl :font-family)
                      font-size   (/ (d/px lbl :font-size) h)
                      color       (d/style lbl :color)
                      text        (.. lbl -value)
                      id          (.. lbl -id)
                      static      (:static (get labels id))]

                  {:pos [x y]
                   :text text
                   :id id
                   :static static
                   :font-family font-family
                   :font-size   font-size
                   :color       color})))))




(defn dimmer []
  (r/with-let [this  (r/current-component)
               body  (sel1 :body)
               close #(dispatch [::evt/update [:dim] false])
               esc   #(when (= (.. % -keyCode) 27) (close))
               _     (d/listen! body :keyup esc)]
    (r/create-class
     {:display-name "dimmer"
      :component-did-mount
      (fn [this]
        (when-let [dom (r/dom-node this)]
          ;; move to top page (0,0)
          (let [top  (- (.. dom getBoundingClientRect -top))
                left (- (.. dom getBoundingClientRect -left))]
            (d/set-px! dom :top top)
            (d/set-px! dom :left left))))
      :reagent-render
      (fn []
        (into
         [:div#dimmer {:on-click close}]
         (r/children this)))})
    (finally
      (d/unlisten! body :keyup esc))))




(defn font-picker [labels]
  (r/with-let [lbls (export-labels labels)]
    [dimmer
     (into
      [:div.picker-container]
      (for [font-family coverton.fonts/font-names]
        [picker-block {:key    font-family
                       :labels lbls
                       :font-family font-family}]))]))




(defn toolbox-font-picker [{:keys [id]}]
  [:div.label-toolbox-mark
   {:style {:background-color "green"}
    :on-click #(do (dispatch [::evt/update-mark id [:static] false])
                   (dispatch [::evt/update [:dim] :show-font-picker]))}])




(defn toolbox [props]
  [:div.label-toolbox.cancel-drag
   [toolbox-font-picker props]])

