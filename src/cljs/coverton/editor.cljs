(ns coverton.editor
  (:require [reagent.core :as r]
            [dommy.core :as d :refer-macros [sel1]]
            [coverton.components :as cc]))

(enable-console-print!)

(defn editor []
  (r/with-let [labels (r/atom nil)]
    (into
     [:div {:on-blur (fn [e]
                       (let [text (.. e -target -value)
                             id (uuid (.. e -target -id))]
                         (when (empty? text)
                           (swap! labels (fn [coll]
                                           (remove #(= id (:uuid %)) coll))))))}
      [:div.editor
       {:on-click (fn [e]
                    (let [rect (.. e -target -parentElement  getBoundingClientRect)
                          px (.. rect -left)
                          py (.. rect -top)]
                      (swap! labels conj
                             {:x (- (- (.. e -clientX) px) 5)
                              :y (- (- (.. e -clientY) py) 5)
                              :uuid (random-uuid)
                              :dom (atom nil)})))}]]
     (->> @labels
          (map (fn [l]
                 ^{:key (:uuid l)}
                 [:div.label-container {:style {:left (:x l) :top (:y l)}}
                  [cc/draggable {:cancel ".cancel-drag"}

                   [cc/toolbox l]
                   
                   [cc/resizable l
                    [cc/autosize-input (assoc l :ref #(reset! (:dom l) %)) ]]]]))
          doall))))
