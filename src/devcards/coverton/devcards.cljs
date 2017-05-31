(ns coverton.devcards
  (:require [reagent.core :as r]
            [devcards.core :as dc]
            [coverton.components :as cc]
            [coverton.editor :as ed]
            [dommy.core :as d :refer-macros [sel1]])
  (:require-macros [devcards.core :refer [defcard-rg]]))


(defonce dc-labels (r/atom {:img {:src "assets/img/coverton.jpg"}
                        :labels []}))

(defcard-rg editor
  [ed/editor dc-labels]
  dc-labels
  {:inspect-data true})


(defcard-rg font-picker
  [cc/font-picker dc-labels]
  ed/dc-font-family
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
