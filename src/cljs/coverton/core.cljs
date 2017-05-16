(ns coverton.core
  (:require [reagent.core :as r]
            [dommy.core :as d :refer-macros [sel1]]
            [hipo.core :as h]))

(def app-state (r/atom {:controls []
                        :idx 0}))

(defn add-label [state]
  (swap! state update-in [:idx] inc)
  (let [idx (:idx @state) ]
    (swap! state update-in [:controls] conj {:text "hello"
                                             :id idx})))

(defn editor [state]
  [:div.editor
   [:div.editor-image {:on-click (fn [e] (add-label state))}
    [:img {:src "assets/img/coverton.jpg"}]]
   (into [:div] (for [c (:controls @state)]
                  ^{:key (:id c)}
                  [:div.editor-control (:text c)]))])


(defn ^:export init []
  (when js/document
    (do
      (r/render [editor app-state] (sel1 :.app)))))
