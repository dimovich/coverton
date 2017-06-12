(ns coverton.ed.views
  (:require [reagent.core  :as r]
            [re-frame.core :as rf :refer [subscribe dispatch dispatch-sync]]
            [coverton.components  :as cc]
            [coverton.ed.events   :as evt]
            [coverton.ed.subs     :as sub]
            [dommy.core :as d]))


(defn item [id]
  (r/with-let [text  (subscribe [::sub/item-text        id])
               pos   (subscribe [::sub/item-pos         id])
               font  (subscribe [::sub/item-font-family id])
               size  (subscribe [::sub/item-font-size   id])]
    
    [cc/draggable {:update-fn #(dispatch [::evt/update-item id [:pos] %])
                   :pos       @pos}
     
     [cc/toolbox {:id id}]
     
     [cc/resizable {:font-size  @size
                    :update-fn  #(dispatch [::evt/update-item id [:font :font-size] %])}
      
      [cc/autosize-input {:id          id
                          :key         :input
                          :text        @text
                          :font-family @font
                          :update-fn   #(dispatch [::evt/update-item id [:text] %])}]]]))



(defn items [ids]
  (into [:div]
        (for [id ids]
          ^{:key id}
          [item id])))




(defn editor []
  (r/with-let [_    (dispatch-sync [::evt/initialize])
               dim  (subscribe [::sub/dim])
               its  (subscribe [::sub/items])
               ids  (subscribe [::sub/item-ids])]

    [:div.editor {:on-blur evt/handle-remove-item}

     [:img.editor-img {:src "assets/img/coverton.jpg"
                       :on-click evt/handle-add-item}]

     (condp = @dim
       :show-font-picker [cc/font-picker @its]
       [items @ids])]))

