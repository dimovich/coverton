(ns coverton.editor
  (:require [reagent.core :as r]
            [dommy.core :as d :refer-macros [sel1]]
            [coverton.components :as cc]))

(enable-console-print!)

;; export
(defn export-labels [labels]
  (->> labels
       (map (fn [[uuid {:keys [dom]}]]
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
                 :font {:font-family (d/style @dom :font-family)
                        :font-size fs
                        :color (d/style @dom :color)}})))
       doall
       (assoc-in {:img {:src "assets/img/coverton.jpg"}}
                  [:labels])))


(defn editor [label-data]
  (r/with-let [labels (r/atom nil)
               this   (r/current-component)
               picker (fn [e]
                        (reset! label-data {:data (export-labels @labels)
                                            :parent this}))]
    (into
     [:div.editor
      { ;; delete empty labels
       :on-blur (fn [e]
                  (let [text (.. e -target -value)
                        id   (uuid (.. e -target -id))]
                    (when (empty? text)
                      (swap! labels dissoc id))))}

      [:div {:style {:position :absolute
                     :right 0 :top 0 :width 50 :height 50
                     :background-color "orange"}
             :on-click picker}]

      
      [:img.editor-img
       {:src "assets/img/coverton.jpg"
        :on-click
        ;; create new label
        (fn [e]
          (let [rect (.. e -target -parentElement  getBoundingClientRect)
                px (.. rect -left)
                py (.. rect -top)]
            (swap! labels assoc
                   (random-uuid) {:x (- (- (.. e -clientX) px) 5)
                                  :y (- (- (.. e -clientY) py) 5)
                                  :dom (atom nil)})))}]]
     
     ;; display labels
     (->> @labels
          (map (fn [[uuid {:keys [dom x y]}]]

                 [:div.label-container {:key uuid
                                        :style {:left x :top y}}
                  [cc/draggable {:cancel ".cancel-drag"
                                 :key :draggable}

                   [cc/toolbox {:dom dom :key :toolbox
                                :data-fn picker}]

                   [cc/resizable {:dom dom, :key :resizable}

                    [cc/autosize-input {:uuid uuid :key :input
                                        :ref #(reset! dom %)}]]]]))))))



