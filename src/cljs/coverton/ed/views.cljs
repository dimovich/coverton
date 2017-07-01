(ns coverton.ed.views
  (:require [reagent.core  :as r]
            [re-frame.core :as rf :refer [subscribe dispatch dispatch-sync]]
            [coverton.components  :as cc]
            [coverton.ed.events   :as evt]
            [coverton.ed.subs     :as sub]
            [ajax.core            :as ajax :refer [GET]]))


(defn mark [id]
  (r/with-let [text  (subscribe [::sub/mark-text        id])
               pos   (subscribe [::sub/mark-pos         id])
               font  (subscribe [::sub/mark-font-family id])
               size  (subscribe [::sub/mark-font-size   id])]
    
    [cc/draggable {:update-fn #(dispatch [::evt/update-mark id [:pos] %])
                   :pos       @pos}
     
     [cc/toolbox {:id id}]
     
     [cc/resizable {:font-size  @size
                    :update-fn  #(dispatch [::evt/update-mark id [:font-size] %])}
      
      [cc/autosize-input {:id          id
                          :key         :input
                          :text        @text
                          :font-family @font
                          :update-fn   #(dispatch [::evt/update-mark id [:text] %])}]]]))



(defn marks [ids]
  (into [:div]
        (for [id ids]
          ^{:key id}
          [mark id])))




(defn editor []
  (r/with-let [;;_    (dispatch-sync [::evt/initialize])
               dim  (subscribe [::sub/dim])
               mrks (subscribe [::sub/marks])
               ids  (subscribe [::sub/mark-ids])]

    [:div.editor {:on-blur evt/handle-remove-mark}

     [:img.editor-img {:src "assets/img/coverton.jpg"
                       :on-click evt/handle-add-mark}]

     (condp = @dim
       :show-font-picker [cc/font-picker @mrks]
       [marks @ids])]))

