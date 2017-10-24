(ns coverton.components
  (:require [reagent.core   :as r]
            [re-frame.core  :as rf :refer [dispatch subscribe]]
            [dommy.core     :as d  :refer [sel1]]
            [taoensso.timbre :refer-macros [info]]
            [coverton.util  :refer [arc]]
            [coverton.fonts :refer [default-font]]
            [coverton.ed.events :as evt]
            [coverton.ed.subs   :as sub]))




(defn cover-block
  [cover & [params]]
  
  [:div.cover-block {:ref #(when % (d/set-px! % :height (d/px % :width)))}
   [:div.cover-block-clickable params]
   [:div.cover-block-svg
    {:dangerouslySetInnerHTML
     {:__html (get-in cover [:cover/fabric :svg])}}]])




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
        [:div.cover-block-box]))]))





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




(defn image-picker []
  [:span
   [:a {:on-click #(.click (sel1 :#image-input))}
    "image"]
   [:form {:style {:display :none}}
    [:input#image-input
     {:type "file"
      :accept "image/*"
      :on-change #(evt/set-image-url
                   (.createObjectURL js/URL (-> % .-target .-files (aget 0))))
      :style {:display :none
              :position :inline-block}}]]])


