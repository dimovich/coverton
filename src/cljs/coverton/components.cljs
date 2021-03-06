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
  [params cover]
  
  [:div.cover-block.clickable
   [:div.cover-block-clickable params [:span "edit me"]]
   [:div.cover-block-svg
    {:dangerouslySetInnerHTML
     {:__html (get-in cover [:cover/fabric :svg])}}]])




(defn dimmer []
  (r/with-let [this  (r/current-component)
               body  (sel1 :body)
               close identity
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
   [:span.menu
    [:span.helper-valign]]
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




(defn image-picker [{:keys [callback]}]
  (r/with-let [input-dom (atom nil)]
    [:span.clickable {:on-click #(some-> @input-dom .click)
                      :style {:width "100%" :height "100%"
                              :position :absolute :top 0 :left 0}}
     [:input#image-input
      {:type "file"
       :accept "image/*"
       :ref #(some->> % (reset! input-dom))
       :on-change #(let [file (-> % .-target .-files (aget 0))]
                     (some->> file
                              (.createObjectURL js/URL)
                              (callback file)))
       :style {:display :none
               :position :inline-block}}]]))

