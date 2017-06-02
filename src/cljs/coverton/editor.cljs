(ns coverton.editor
  (:require [reagent.core :as r]
            [re-frame.core :as rf :refer [subscribe]]
            [coverton.components :as cc]
            [coverton.editor.events :as events]
            [coverton.editor.subs]
            [coverton.editor.db]))


(defn editor []
  (r/with-let [items (subscribe [:items])]
    (into
     [:div.editor {:on-blur events/handle-remove-item}
      [:div {:style {:position :absolute
                     :right 0 :top 0 :width 50 :height 50
                     :background-color "orange"}}]

      
      [:img.editor-img
       {:src "assets/img/coverton.jpg"
        :on-click events/handle-add-item}]]
     
     ;; display labels
     (->> @items
          (map (fn [[id {:keys [x y dimmed]}]]

                 [:div.label-container {:key id :style {:left x :top y}}
                  [cc/draggable {:cancel ".cancel-drag"
                                 :key    :draggable
                                 :id     id}
                   [cc/toolbox {:id id}]
                   [cc/resizable {:id id :key :resizable}
                    [cc/autosize-input {:id id :key :input}]]]]))))))

