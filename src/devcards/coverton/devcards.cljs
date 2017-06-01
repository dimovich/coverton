(ns coverton.devcards
  (:require [reagent.core :as r]
            [devcards.core :as dc]
            [coverton.components :as cc]
            [coverton.editor :as ed])
  (:require-macros [devcards.core :refer [defcard-rg]]))


(def dc-labels (r/atom nil))

(defcard-rg editor
  [ed/editor dc-labels])


(defcard-rg font-picker
  [cc/font-picker dc-labels]
  dc-labels
  {:inspect-data true})


#_(defcard-rg label
    (fn [data-atom _]
      [cc/draggable {:dom data-atom
                     :cancel ".cancel-drag"}
       [cc/toolbox {:dom data-atom}]
       [cc/resizable {:dom data-atom}
        [cc/autosize-input {:ref #(reset! data-atom %)
                            :uuid 143434}]]])
    (atom nil))



(defn ^:export init []
  (dc/start-devcard-ui!))



;; 
