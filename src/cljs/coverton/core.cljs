(ns coverton.core
  (:require [reagent.core :as r]
            [dommy.core :as d :refer-macros [sel1]]
            [komponentit.autosize :as autosize]
            [cljsjs.react-drag]))

(enable-console-print!)

;;(def draggable identity)

(def react-drag (r/adapt-react-class js/ReactDrag))

(defn vec-remove
  "remove elem in coll"
  [coll pos]
  (vec (concat (subvec coll 0 pos) (subvec coll (inc pos)))))


(defn get-xy [el]
  [(-> (sel1 el) .getBoundingClientRect .-left)
   (-> (sel1 el) .getBoundingClientRect .-top)])


(defn autosize-input [{:keys [x y uuid]}]
  (r/with-let [state (r/atom nil)]
    [:div.label-container
     {:style {:left x
              :top y}}
     [react-drag #_{:on-start ;;get dom, and put focus on me
                    :on-stop  ;; put focus back on child
                    }
      [:div
       [autosize/input {:value @state
                        :on-change (fn [e] (reset! state (.. e -target -value)))
                        :class :editor-label
                        :id uuid
                        :auto-focus true}]]]]))



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

     (->> @labels
          (map (fn [l]
                 ^{:key (:uuid l)}
                 [autosize-input l]))
          doall))))



(defn ^:export init []
  (when js/document
    (do
      (r/render [editor] (sel1 :.app)))))

