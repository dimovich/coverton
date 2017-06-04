(ns coverton.devcards
  (:require [reagent.core :as r]
            [re-frame.core :as rf :refer [subscribe]]
            [devcards.core :as dc]
            [coverton.components :as cc]
            [coverton.editor :as ed]
            [coverton.editor.subs])
  (:require-macros [devcards.core :refer [defcard-rg]]))


(def items (subscribe [:items]))


(defcard-rg editor
  [ed/editor]
  items
  {:inspect-data true})


#_(defcard-rg font-picker
    (fn [data-atom _]
      [cc/font-picker @data-atom])
    items
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


(defn reload []
  (rf/dispatch-sync [:initialize]))

(defn ^:export init []
  (dc/start-devcard-ui!))

