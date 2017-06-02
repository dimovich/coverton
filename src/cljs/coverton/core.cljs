(ns coverton.core
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [dommy.core :as d :refer-macros [sel1]]
            [coverton.editor :as ed]))

(enable-console-print!)


(defn reload []
  (rf/dispatch-sync [:initialize])
  (r/render [ed/editor] (sel1 :#app)))



(defn ^:export main []
  (reload))



;;[ContainerDimensions {} (fn [height] (r/as-element [my-component {:height height}]))]
