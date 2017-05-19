(ns coverton.core
  (:require [reagent.core :as r]
            [dommy.core :as d :refer-macros [sel1]]
            [komponentit.autosize :as autosize]))


(defn vec-remove
  "remove elem in coll"
  [coll pos]
  (vec (concat (subvec coll 0 pos) (subvec coll (inc pos)))))


(defn dom-pos [el]
  [(-> (sel1 el) .getBoundingClientRect .-left)
   (-> (sel1 el) .getBoundingClientRect .-top)])


(defn add-label [labels]
  (let [c (count @labels)]
    (swap! labels conj {:text "Hello"})))


(defn editor []
  (let [labels (r/atom [])
        focus (r/atom false)]
    (r/create-class
     {:display-name "cover-editor"
      :reagent-render
      (fn []
        [:div.editor
         [:img.editor-image {:src "assets/img/coverton.jpg"
                             :on-click (fn [e]
                                         (let [[px py] (dom-pos :.editor)]
                                           (swap! labels conj {:text ""
                                                               :x (- (.-clientX e) px)
                                                               :y (- (.-clientY e) py)}))
                                         (reset! focus true))}]
         ;; labels
         (doall
          (map-indexed
           (fn [idx item]
             (let [v (r/cursor labels [idx :text])]
               ^{:key (str "label" idx)}
               [autosize/input {:value @v
                                :on-change (fn [e] (reset! v (.. e -target -value)))
                                :on-blur (fn [e] (when (empty? @v)
                                                   (swap! labels vec-remove idx)))
                                :class :editor-label
                                :id (str "label" idx)
                                :style {:left (:x item)
                                        :top (:y item)}}]))
           @labels))])
      :component-did-update
      (fn []
        (when @focus
          (do
            (.focus (sel1 (str "#label" (dec (count @labels)))))
            (reset! focus false))))})))



(defn ^:export init []
  (when js/document
    (do
      (r/render [editor] (sel1 :.app)))))



;; FIXME: input cursor jumps to the end
