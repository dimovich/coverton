(ns coverton.editor
  (:require [reagent.core :as r]
            [dommy.core :as d :refer-macros [sel1]]
            [coverton.components :as cc]))

(enable-console-print!)

(defn editor []
  (r/with-let [labels (r/atom nil)]
    [:div {;; delete empty labels
           :on-blur (fn [e]
                      (let [text (.. e -target -value)
                            id   (uuid (.. e -target -id))]
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
                             :dom (atom nil)})))}]

     (->> @labels
          (map (fn [{:keys [uuid dom x y]}]
                 [:div.label-container {:style {:left x :top y}
                                        :key uuid}
                  [cc/draggable {:cancel ".cancel-drag"
                                 :key :draggable}

                   [cc/toolbox {:dom dom, :key :toolbox}]
                  
                   [cc/resizable {:dom dom, :key :resizable}
                    [cc/autosize-input {:key :input, :uuid uuid
                                        :ref #(reset! dom %)}]]]]))
          doall)]))
