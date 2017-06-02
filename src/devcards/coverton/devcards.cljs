(ns coverton.devcards
  (:require [reagent.core :as r]
            [re-frame.core :as rf :refer [subscribe]]
            [devcards.core :as dc]
            [coverton.components :as cc]
            [coverton.editor :as ed]
            [coverton.editor.subs])
  (:require-macros [devcards.core :refer [defcard-rg]]))


(def items-with-dom (subscribe [:items-with-dom]))


(defcard-rg editor
  [ed/editor])


(defcard-rg font-picker
  (fn [data-atom _]
   [cc/font-picker (cc/export-labels @data-atom)])
  items-with-dom
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
  (rf/dispatch-sync [:initialize])
  (dc/start-devcard-ui!))

