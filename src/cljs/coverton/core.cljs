(ns coverton.core
  (:require [reagent.core :as r]
            [dommy.core :as d :refer-macros [sel1]]
            [coverton.editor :as ed]))


(enable-console-print!)


(defn ^:export init []
  (when js/document
    (do
      (r/render [ed/editor] (sel1 :.app)))))



;; TODO
;;
;; fade-in fade-out of border
;;

;;[ContainerDimensions {} (fn [height] (r/as-element [my-component {:height height}]))]
