(ns coverton.components
  (:require [reagent.core   :as r]
            [re-frame.core  :as rf :refer [dispatch subscribe]]
            [dommy.core     :as d  :refer-macros [sel1 sel]]
            [coverton.util  :refer [arc info]]
            [coverton.fonts :refer [default-font]]
            [coverton.ed.events :as evt]
            [coverton.ed.subs   :as sub]
;;            [jsutils]
            ))


(def react-drag   (arc "deps" "draggable"))
(def react-resize (arc "deps" "resizable"))
(def Button       (arc "deps" "semui" "Button"))
(def Input        (arc "deps" "semui" "Input"))
(def react-color  (arc "deps" "react-color"))


;;(def resize-detector ((goog.object/getValueByKeys js/window "deps" "resize-detector")))


;;
;; calculate text width in px for font type and size
;; and change element width
;;
(defn set-width [el]
  (let [font (d/style el :font-family)
        size (d/style el :font-size)
        span (sel1 :#span-measure)]

    ;; copy styles to span
    (d/set-style! span :font-size size)
    (d/set-style! span :font-family font)
    (d/set-html!  span "")
    (d/append!    span (d/create-text-node (d/value el))) ;;fixme: memleak?

    ;; get normal width (has issues with whitespace),
    ;; so possibly extend to scroll width
    (d/set-px! el :width (+ 2 (.. span -scrollWidth)))
    (d/set-px! el :width (.. el -scrollWidth))))



(defn autosize-input
  [{:keys [id update-fn text font-family color set-ref read-only?]}]
  
  (let [state          (r/atom (or text ""))
        caret-pos      (atom 0)
        update-width   #(set-width (r/dom-node %))
        blur           #(.. % -target blur)

        enable-static  #(do
                          (evt/set-mark-read-only id true)
                          ;;(reset! caret-pos (jsutils/getCaretPosition id))
                          )
        disable-static #(do ;;fixme doesn't focus
                          (evt/set-mark-read-only id false)
                          ;;(jsutils/setCaretPosition id @caret-pos)
                          )]
    
    (r/create-class
     {:display-name "autosize-input"
      
      :component-did-mount
      (fn [this]
        ;;for the outer component who modifies size or attributes
        (set-ref (r/dom-node this))
        ;;(evt/set-ref id (r/dom-node this))
        (update-width this))

      :component-did-update update-width
      
      :reagent-render
      (fn [{:keys [font-family color read-only?]}]
        (let [common {:id id
                      :value @state
                      :on-click #(evt/set-active-mark id)
                      :style {:font-size   "1em"
                              :font-family font-family
                              :color       color}}
              
              editable {:on-change #(reset! state (.. % -target -value))
                        :on-blur   #(do
                                      (enable-static)
                                      (update-fn @state))
                        :on-key-down (fn [e]
                                       (condp = (.. e -key)
                                         "Enter" (blur e)
                                         "Escape" (blur e)
                                         "Delete" #(do (reset! state "") ;;delete value ;; move to static
                                                       (blur e)) ;; delete
                                         false))

                        :class "mark-input cancel-drag"
                        :auto-focus true}
              
              static {:on-double-click disable-static
                      :read-only true
                      :on-focus blur
                      :class "mark-input"
                      :style {:cursor :move}}]
          
          [:input
           (merge-with merge common (if read-only? static editable))]))})))




(defn resizable [{:keys [font-size update-fn ref]}]
  (r/with-let [this  (r/current-component)
               start (atom nil)
               size  (atom nil)]
    (into
     [react-resize {:class-name "mark-resize"
                    :width "1em" :height "1em"
                    :style {:font-size  font-size}
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

                                 ;; update child
                                 (set-width @ref))}]
     (r/children this))))



;; move draggable to resizable? and try to see if the no-item error still appears
(defn draggable [{:keys [update-fn start-pos ref]}]
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
        (let [img (.. (sel1 :.picker-img)
                      getBoundingClientRect)
              w (.. img -width)
              h (.. img -height)]
          (reset! size [w h])))
      
      :reagent-render
      (fn []
        (let [marks (:marks cover)
              block-family font-family
              [offset-x offset-y] @(subscribe [::sub/mark-offset])]
          (into
           [:div.picker-block
            [:img.picker-img {:src (:image-url cover)}]]
       
           (->> marks
                (map (fn [[_ {:keys [mark-id pos text font-size font-family color static]}]]
                       (let [id (str mark-id)
                             [w h] @size
                             font-size (* font-size h)
                             [x y] pos
                             x (* x w)
                             y (* y h)]

                         ^{:key id}
                         [:input.picker-mark
                          {:value text
                           :on-focus #(.. % -target blur)
                           :on-click #(do (evt/set-font-family id block-family)
                                          (evt/set-mark-static id true))
                           
                           :style {:font-family (if static font-family block-family)
                                   :font-size font-size
                                   :read-only true
                                   :color color
                                   :left x
                                   :top  y}}
                          
                          ;;text
                          ])))))))})))



(defn dimmer []
  (r/with-let [this  (r/current-component)
               body  (sel1 :body)
               close #(evt/set-dimmer nil)
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
  (r/with-let [cover (sub/export-cover)]
    [dimmer
     (into
      [:div.picker-container]
      (for [font-family coverton.fonts/font-names]
        ^{:key font-family}
        [picker-block {:cover cover
                       :font-family font-family}]))]))




(defn toolbox-font-picker [{:keys [id]}]
  [:div.mark-toolbox-wrap
   {:style {:background-color "green"}
    :on-click #(do (evt/set-mark-static id false)
                   (evt/set-dimmer :font-picker))}])



(defn toolbox-color-picker [{:keys [id ref]}]
  (let [update-color #(d/set-px! ref :font-color (str "#" %))]
    [:div.mark-toolbox-wrap
     {:style {:background-color "#FF9933"}}]))



(defn toolbox [props]
  [:div.mark-toolbox.cancel-drag
   [toolbox-font-picker props]])



(defn color-picker []
  (r/with-let [id           (subscribe [::sub/active-mark])
               active-color (subscribe [::sub/active-color])
               set-color    #(when @id
                               (evt/set-color @id ((js->clj %) "hex")))]
    
    [react-color {:on-change set-color ;;:on-change-complete
                  :color @active-color}]))


