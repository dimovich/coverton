(ns coverton.editor
  (:require [reagent.core :as r]
            [re-frame.core :as rf :refer [subscribe dispatch]]
            [coverton.components :as cc]
            [coverton.editor.events :as events]
            [coverton.editor.subs]
            [coverton.editor.db]
            [dommy.core :as d]))



(defn item [[id {:keys [pos text font]}]]
  (r/with-let [[x y]  pos]
    [cc/draggable {:cancel ".cancel-drag"
                   :key    :draggable
                   :id     id
                   :pos    pos}
     [cc/toolbox {:id id}]
     [cc/resizable {:id id}
      [cc/autosize-input {:id   id
                          :key  :input
                          :text text
                          :update-fn  #(dispatch [:update-item id [:text] %])}]]]))




(defn editor []
  (r/with-let [items (subscribe [:items])
               dim   (subscribe [:dim])]
    (into
     [:div.editor {:on-blur events/handle-remove-item}

      [:img.editor-img
       {:src "assets/img/coverton.jpg"
        :on-click events/handle-add-item}]]

     (condp = @dim
       :show-font-picker
       (let [labels (cc/export-labels @items)]
         [[cc/font-picker labels]])
       
       ;; display labels
       (for [lbl @items]
         [item lbl])))))



