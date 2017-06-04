(ns coverton.editor
  (:require [reagent.core :as r]
            [re-frame.core :as rf :refer [subscribe dispatch]]
            [coverton.components :as cc]
            [coverton.editor.events :as events]
            [coverton.editor.subs]
            [coverton.editor.db]
            [dommy.core :as d]))


(defn item [id]
  (r/with-let [lbl (subscribe [:item id])]
    (let [{:keys [pos text]} @lbl]
      [cc/draggable {:cancel ".cancel-drag"
                     :id     id
                     :pos    pos}
       
       [cc/toolbox {:id  id}]
       
       [cc/resizable {:id  id}
        
        [cc/autosize-input {:id   id
                            :key  :input
                            :text text
                            :update-fn #(dispatch [:update-item id [:text] %])}]]])))


(defn items []
  (r/with-let [ids (subscribe [:item-ids])]
    (into [:div]
          (for [id @ids]
            ^{:key id}
            [item id]))))




(defn editor []
  (r/with-let [dim      (subscribe [:dim])
               changed? (subscribe [:count])
               its      (subscribe [:items])]

    [:div.editor {:on-blur events/handle-remove-item}

     [:img.editor-img
      {:src "assets/img/coverton.jpg"
       :on-click events/handle-add-item}]

     (condp = @dim
       :show-font-picker
       (let [labels (cc/export-labels @its)]
         [cc/font-picker labels])

       [items])]))



