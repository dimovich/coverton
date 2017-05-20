(ns coverton.core
  (:require [reagent.core :as r]
            [dommy.core :as d :refer-macros [sel1]]
            [komponentit.autosize :as autosize]))

(enable-console-print!)

(defn vec-remove
  "remove elem in coll"
  [coll pos]
  (vec (concat (subvec coll 0 pos) (subvec coll (inc pos)))))


(defn get-xy [el]
  [(-> (sel1 el) .getBoundingClientRect .-left)
   (-> (sel1 el) .getBoundingClientRect .-top)])


(defn autosize-input [{:keys [x y uuid]}]
  (let [state (r/atom nil)]
    (r/create-class
     {:display-name "coverton.autosize-input"
      :reagent-render
      (fn []
        [autosize/input {:value @state
                         :on-change (fn [e]
                                      (reset! state (.. e -target -value)))
                         :class :editor-label
                         :id uuid
                         :style {:left x
                                 :top y}}])
      ;; put focus on input
      :component-did-mount
      (fn [self _]
        (-> self r/dom-node .focus))})))



(defn editor []
  (r/with-let [labels (r/atom nil)]
    (into
     [:div.editor
      ;; delete empty labels
      {:on-blur (fn [e]
                  (let [text (.. e -target -value)
                        id (uuid (.. e -target -id))]
                    (when (empty? text)
                      (swap! labels (fn [coll]
                                      (remove #(= id (:uuid %)) coll))))))}
    
      [:img.editor-image {:src "assets/img/coverton.jpg"
                          :on-click (fn [e]
                                      (let [[px py] (get-xy :.editor)
                                            id (random-uuid)]
                                        (swap! labels conj
                                               {:x (- (.-clientX e) px)
                                                :y (- (.-clientY e) py)
                                                :uuid id})))}]]
     (doall
      (for [l @labels]
        ^{:key (:uuid l)}
        [autosize-input l])))))



(defn ^:export init []
  (when js/document
    (do
      (r/render [editor] (sel1 :.app)))))

