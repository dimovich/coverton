(ns coverton.editor.views
  (:require [reagent.core :as r]
            [re-frame.core :as rf :refer [subscribe]]
            [coverton.editor.events :as events]
            [coverton.editor.subs]
            [coverton.editor.db]
            [coverton.components :as cc]))



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
          (map (fn [[id {:keys [x y]}]]

                 [:div.label-container {:key id :style {:left x :top y}}
                  [cc/draggable {:cancel ".cancel-drag"
                                 :key :draggable}
                   [cc/toolbox {:id id}]
                   [cc/resizable {:id id}
                    [cc/autosize-input {:id id}]]]]))))))

