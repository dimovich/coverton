(ns coverton.util
  (:require [reagent.core :as r]))



(defn info [& args]
  ;;(enable-console-print!)
  (apply println args))


(defn arc [& args]
  (r/adapt-react-class
   (apply goog.object/getValueByKeys js/window args)))
