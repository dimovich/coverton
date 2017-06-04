(ns coverton.editor
  (:require [reagent.core :as r]
            [re-frame.core :as rf :refer [subscribe dispatch]]
            [coverton.components :as cc]
            [coverton.editor.events :as events]
            [coverton.editor.subs]
            [coverton.editor.db]
            [dommy.core :as d]))


(defn item [id]
  (r/with-let [text  (subscribe [:ed-item-text        id])
               pos   (subscribe [:ed-item-pos         id])
               font  (subscribe [:ed-item-font-family id])
               size  (subscribe [:ed-item-font-size   id])
               [x y] @pos]
    
    [cc/draggable {:update-fn #(dispatch [:update-item id [:pos] %])
                   :pos       @pos}
     
     [cc/toolbox {:id  id}]
     
     [cc/resizable {:font-size  @size
                    :update-fn  #(dispatch [:update-item id [:font :font-size] %])}
      
      [cc/autosize-input {:id          id
                          :key         :input
                          :text        @text
                          :font-family @font
                          :update-fn   #(dispatch [:update-item id [:text] %])}]]]))



(defn items [ids]
  (into [:div]
        (for [id ids]
          ^{:key id}
          [item id])))




(defn editor []
  (r/with-let [dim  (subscribe [:ed-dim])
               its  (subscribe [:ed-items])
               ids  (subscribe [:ed-item-ids])]

    [:div.editor {:on-blur events/handle-remove-item}

     [:img.editor-img {:src "assets/img/coverton.jpg"
                       :on-click events/handle-add-item}]

     (condp = @dim
       :show-font-picker
       [cc/font-picker @its]

       [items @ids])]))



