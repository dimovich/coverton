(ns coverton.core
  (:require [reagent.core :as r]
            [dommy.core :as d :refer-macros [sel1]]
            [komponentit.autosize :as autosize]))

(enable-console-print!)

;;(set! *warn-on-infer* true)


(def react-drag (r/adapt-react-class (aget js/window "deps" "react-drag")))
(def react-resizable (r/adapt-react-class (aget js/window "deps" "react-resizable")))

(defn get-xy [el]
  [(.. (sel1 el) getBoundingClientRect -left)
   (.. (sel1 el) getBoundingClientRect -top)])


(defn autosize-input [{:keys [uuid]}]
  (r/with-let [state (r/atom nil)]
    [autosize/input {:value @state
                     :on-change (fn [e] (reset! state (.. e -target -value)))
                     :class :editor-label
                     :id uuid
                     :auto-focus true}]))



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
                                               {:x (- (.. e -clientX) px)
                                                :y (- (.. e -clientY) py)
                                                :uuid id})))}]]

     (->> @labels
          (map (fn [l]
                 ^{:key (:uuid l)}
                 [:div.label-container {:style {:left (:x l)
                                                :top (:y l)}}

                  [react-drag {:handle :.drag-me}
                   [:div
                    [:div.drag-me {:style {:padding 10
                                           :resize "both"}}
                     [autosize-input l]]]]]))
          doall))))



(defn ^:export init []
  (when js/document
    (do
      (r/render [editor] (sel1 :.app)))))

