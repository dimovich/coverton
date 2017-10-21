(ns coverton.components
  (:require [reagent.core   :as r]
            [re-frame.core  :as rf :refer [dispatch subscribe]]
            [dommy.core     :as d  :refer-macros [sel1]]
            [taoensso.timbre :refer-macros [info]]
            [coverton.util  :refer [arc]]
            [coverton.fonts :refer [default-font]]
            [coverton.ed.events :as evt]
            [coverton.ed.subs   :as sub]))


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
        (let [marks (:cover/marks cover)
              block-family font-family
              [offset-x offset-y] @(subscribe [::sub/mark-offset])]
          (into
           [:div.picker-block
            [:img.picker-img {:src (:cover/image-url cover)}]]
       
           (->> marks
                (map (fn [[_ {:keys [id pos text font-size font-family color static]}]]
                       (let [id (str id)
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
                                   :top  y}}])))))))})))





(defn cover-image [{url :url size :size}]
  (r/with-let [this        (r/current-component)
               update-size (fn [e]
                             (->> (.. e -target getBoundingClientRect)
                                  ((juxt #(.. % -width) #(.. % -height)))
                                  (reset! size)))]
    
    [:img.cover-image {:on-load  update-size
                       :src      url}]))




(defn cover-mark [{:keys [id pos text
                          font-size font-family
                          color static parent-size]}]
  (let [id (str id) ;;mark-id collision?
        [w h] parent-size
        font-size (* font-size h)
        [x y] pos
        x (* x w)
        y (* y h)]

    ^{:key id}
    [:input.cover-mark {:value text
      :style {:font-family font-family
              :font-size font-size
              :read-only true
              :color color
              :left x
              :top  y}}]))




(defn cover-block [cover & [params]]
  (r/with-let [size (r/atom nil)]
    [:div.cover-block (merge params)
     [cover-image {:url (:cover/image-url cover)
                   :size size}]
     (doall (->> (:cover/marks cover)
                 vals
                 (map #(cover-mark
                        (merge % {:parent-size @size})))))]))




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
  (r/with-let [cover @(subscribe [::sub/cover])]
    [dimmer
     (into
      [:div.picker-container]
      (for [font-family coverton.fonts/font-names]
        ^{:key font-family}
        [:div.cover-block-box
         [picker-block {:cover cover
                        :font-family font-family}]]))]))




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




(defn menu [& args]
  (into
   [:span.menu]
   (some->> args
            (filter identity)
            (interpose [:span.separator "|"]))))




(defn editable [tag {:keys [state error on-change] :as props}]
  [tag
   (-> props
       (dissoc :state :error)
       (merge (cond-> {:value @state
                       :on-change #(do (reset! state (.. % -target -value))
                                       (when on-change (on-change)))}
                error (merge {:class :error}))))])



(defn error [msgs]
  [:span
   (for [msg msgs]
     [:p.error msg])])




(defn image-picker-button [set-fn]
  [:span
   [:a {:on-click #(.click (sel1 :#image-input))}
    "image"]
   [:input#image-input
    {:type "file"
     :accept "image/*"
     :style {:display :none
             :position :inline-block}
     :on-change #(set-fn (.createObjectURL js/URL (-> % .-target .-files (aget 0))))}]])




(defn form-data [id]
  (when-let [file (some-> (sel1 id)
                          .-files
                          (aget 0))]
    (doto
        (js/FormData.)
        (.append "file" file))))




(defn save-cover [cover]
  (if-let [file (form-data :#image-input)] ;;todo: check if already uploaded
    (dispatch [::evt/upload-file file
               {:on-success [::evt/save-cover cover]}])
    
    (dispatch [::evt/save-cover cover])))

