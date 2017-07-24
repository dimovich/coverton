(ns coverton.components
  (:require [reagent.core   :as r]
            [re-frame.core  :as rf :refer [dispatch subscribe]]
            [dommy.core     :as d  :refer-macros [sel1 sel]]
            [coverton.fonts :refer [default-font]]
            [coverton.ed.events :as evt]
            [coverton.ed.subs   :as sub]))


(enable-console-print!)

(def react-drag   (r/adapt-react-class
                   (goog.object/getValueByKeys js/window "deps" "draggable")))

(def react-resize (r/adapt-react-class
                   (goog.object/getValueByKeys js/window "deps" "resizable")))

;;(def resize-detector ((goog.object/getValueByKeys js/window "deps" "resize-detector")))


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





(defn autosize-input
  [{:keys [id update-fn text font-family ref]}]
  
  (let [state   (r/atom (or text ""))
        update  #(set-width (r/dom-node %) @state)]
    
    (r/create-class
     {:display-name "autosize-input"
      
      :component-did-mount
      (fn [this]
        ;;for the outer component who modifies size or attributes
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
                                        27 (.. e -target blur)    ;; esc
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



(defn draggable [{:keys [update-fn start-pos]}]
  (r/with-let [this  (r/current-component)
               [x y] start-pos]
    [react-drag {:cancel ".cancel-drag"
                 :on-stop (fn [_ d]
                            (let [d (js->clj d)]
                              (update-fn [(+ x (d "x"))
                                          (+ y (d "y"))])))}
     
     (into
      [:div.react-draggable-child]
      (r/children this))]))



;; fixme: pos does not include drag and resize
;;
(defn picker-block [{:keys [cover font-family]}]
  
  (let [size (r/atom nil)]
    (r/create-class
     {:display-name "picker-block"

      :component-did-mount
      (fn [this]
        (let [w (d/px (sel1 :.picker-img) :width)]
          (reset! size [w w])))
      
      :reagent-render
      (fn []
        (let [marks (:marks cover)
              block-family font-family]
          (into
           [:div.picker-block
            [:img.picker-img {:src (:image-url cover)}]]
       
           (->> marks
                (map (fn [{:keys [mark-id pos text font-size font-family color static]}]
                       (let [id (str mark-id)
                             [w h] @size
                             font-size (* font-size h)
                             [x y] pos
                             x (* x w)
                             y (* y h)]

                         ^{:key id}
                         [:span.picker-mark
                          {:on-click #(do (evt/update-font-family id block-family)
                                          (evt/update-mark-static id true))
                           
                           :style {:font-family (if static font-family block-family)
                                   :font-size font-size
                                   :color color
                                   :top y
                                   :left x}}
                          
                          text])))))))})))



(defn dimmer []
  (r/with-let [this  (r/current-component)
               body  (sel1 :body)
               close #(evt/update-dim false)
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




(defn font-picker []
  (r/with-let [cover @(subscribe [::sub/cover])]
    [dimmer
     (into
      [:div.picker-container]
      (for [font-family coverton.fonts/font-names]
        ^{:key font-family}
        [picker-block {:cover cover
                       :font-family font-family}]))]))




(defn toolbox-font-picker [{:keys [id]}]
  [:div.label-toolbox-mark
   {:style {:background-color "green"}
    :on-click #(do (evt/update-mark-static id false)
                   (evt/update-dim :show-font-picker))}])




(defn toolbox [props]
  [:div.label-toolbox.cancel-drag
   [toolbox-font-picker props]])

