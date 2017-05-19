(ns coverton.core
  (:require [reagent.core :as r]
            [dommy.core :as d :refer-macros [sel1]]
            [komponentit.autosize :as autosize]))


(defn vec-remove
  "remove elem in coll"
  [coll pos]
  (vec (concat (subvec coll 0 pos) (subvec coll (inc pos)))))


(defn get-xy [el]
  [(-> (sel1 el) .getBoundingClientRect .-left)
   (-> (sel1 el) .getBoundingClientRect .-top)])


(defn autosize-input [{:keys [x y parent uuid] :as opts}]
  (let [v (r/atom (assoc opts :text ""))]
    (r/create-class
     {:reagent-render
      (fn [{:keys [x y parent uuid] :as opts}]
        ^{:key (:id uuid)}
        [:div
         [autosize/input {:value (:text @v)
                          :on-change (fn [e] (swap! v assoc :text (.. e -target -value)))
                          :class :editor-label
                          :style {:left x
                                  :top y}}]])
      ;; focus
      :component-did-mount
      (fn [self _] (-> self r/dom-node .-firstChild .focus))})))


(defn editor []
  (let [labels (r/atom {})]
    (fn []
      (into
       [:div.editor
        [:img.editor-image {:src "assets/img/coverton.jpg"
                            :on-click (fn [e]
                                        (let [[px py] (get-xy :.editor)
                                              uuid (random-uuid)]
                                          (swap! labels assoc
                                                 uuid [autosize-input
                                                       {:x (- (.-clientX e) px)
                                                        :y (- (.-clientY e) py)
                                                        :uuid uuid
                                                        :parent labels}])))}]]
       (vals @labels)))))



(defn ^:export init []
  (when js/document
    (do
      (r/render [editor] (sel1 :.app)))))

