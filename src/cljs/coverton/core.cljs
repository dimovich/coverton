(ns coverton.core
  (:require [reagent.core :as r]
            [dommy.core :as d :refer-macros [sel1]]
            [coverton.index.views  :as index]))

(enable-console-print!)


(defn app []
  [index/index])


(defn reload []
  (r/render [app] (sel1 :#app)))


(defn ^:export main []
  (reload))



;;[ContainerDimensions {} (fn [height] (r/as-element [my-component {:height height}]))]
