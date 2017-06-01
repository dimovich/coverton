(ns coverton.editor
  (:require [reagent.core :as r]
            [dommy.core :as d :refer-macros [sel1]]
            [coverton.components :as cc]))

(enable-console-print!)



(defn add-label [labels]
  (fn [e]
    (let [rect (.. e -target -parentElement  getBoundingClientRect)
          px (.. rect -left)
          py (.. rect -top)]
      (swap! labels assoc
             (random-uuid) {:x (- (- (.. e -clientX) px) 5)
                            :y (- (- (.. e -clientY) py) 5)
                            :dom (atom nil)
                            :static false}))))


(defn editor [label-data]
  (r/with-let [labels (r/atom nil)
               this   (r/current-component)
               picker (fn [e]
                        (reset! label-data {:labels labels
                                            :parent this}))
               _      (picker)]
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
        :on-click (add-label labels)}]]
     
     ;; display labels
     (->> @labels
          (map (fn [[uuid {:keys [dom x y]}]]

                 [:div.label-container {:key uuid
                                        :style {:left x :top y}}
                  [cc/draggable {:cancel ".cancel-drag"}

                   [cc/toolbox {:dom dom
                                :data-fn picker}]

                   [cc/resizable {:dom dom}

                    [cc/autosize-input {:uuid uuid :ref #(reset! dom %)}]]]]))))))



