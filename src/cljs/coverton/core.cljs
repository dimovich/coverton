(ns coverton.core
  (:require [reagent.core :as r]
            [dommy.core :as d :refer-macros [sel1]]
            [hipo.core :as h]
            [cljsjs.react-select]
            [cljsjs.react-input-autosize]))


(def app-state (r/atom {:controls []
                        :idx 0}))



(def autosize-input (r/adapt-react-class js/AutosizeInput))
(def select (r/adapt-react-class js/Select))


(defn vec-remove
  "remove elem in coll"
  [coll pos]
  (vec (concat (subvec coll 0 pos) (subvec coll (inc pos)))))


(defn add-label [labels]
  (let [c (count @labels)]
    (swap! labels conj {:text "Hello"})))

(defn dom-pos [el]
  [(-> (sel1 el) .getBoundingClientRect .-left)
   (-> (sel1 el) .getBoundingClientRect .-top)])


(defn editor []
  (let [labels (r/atom [])]
    (fn []
      [:div.editor
       [:img.editor-image {:src "assets/img/coverton.jpg"
                           :on-click (fn [e]
                                       (let [[px py] (dom-pos :.editor)]
                                         (swap! labels conj {:text ""
                                                             :x (- (.-clientX e) px)
                                                             :y (- (.-clientY e) py)})))}]
       (map-indexed
        (fn [idx item]
          (let [v (r/cursor labels [idx :text])]
            ^{:key idx}
            [autosize-input {:value @v
                             :on-change #(reset! v (-> % .-target .-value))
                             #_(:on-blur #(when (empty? @v)
                                            (swap! labels vec-remove idx)))
                             :class :editor-label
                             :id (str "label" idx)
                             :style {:left (:x item)
                                     :top (:y item)}}]))
        @labels)])
    #_(r/create-class
       {:display-name "editor"
        :component-did-update
        #(when @focus
           (do
             (-> (sel1 (str "#label" (dec (count @labels))))
                 .focus )
             (reset! focus false)))
        :reagent-render
        (fn []
          [:div.editor
           [:img.editor-image {:src "assets/img/coverton.jpg"
                               :on-click (fn [e]
                                           (let [[px py] (dom-pos :.editor)]
                                             (swap! labels conj {:text ""
                                                                 :x (- (.-clientX e) px)
                                                                 :y (- (.-clientY e) py)})
                                             (reset! focus true)))}]
           (map-indexed
            (fn [idx item]
              (let [v (r/cursor labels [idx :text])]
                ^{:key (:id idx)}
                [autosize-input {:value @v
                                 :on-change #(reset! v (-> % .-target .-value))
                                 #_(:on-blur #(when (empty? @v)
                                                (swap! labels vec-remove idx)))
                                 :class :editor-label
                                 :id (str "label" idx)
                                 :style {:left (:x item)
                                         :top (:y item)}}]))
            @labels)])})))


(defn ^:extern init []
  (when js/document
    (do
      (r/render [editor] (sel1 :.app)))))



