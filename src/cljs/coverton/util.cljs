(ns coverton.util
  (:require [reagent.core :as r]))


(defn info [& args]
  (apply println args))


(defn arc [& args]
  (r/adapt-react-class
   (apply goog.object/getValueByKeys js/window args)))



;;todo: use specter
(defn merge-props [res new]
  (if (map? res)
    (merge-with merge-props res new)
    new))


(defn merge-db [db [m]]
  (merge-with merge-props db m))
