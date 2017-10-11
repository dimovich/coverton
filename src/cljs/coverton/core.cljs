(ns coverton.core
  (:require [reagent.core :as r]
            [dommy.core :as d :refer-macros [sel1]]
            [coverton.index.views  :as index]
            [taoensso.timbre :refer-macros [info]]
            [cljsjs.react-draggable]
            [cljsjs.react-color]
            [cljsjs.fabric]
            [re-resizable]
            [coverton.util :refer [arc]]))


(defn app []
  [index/index])


(defn ^:export reload []
  (info (r/adapt-react-class js/re-resizable))
;;  (info resize/Resizable)
;;  (info color/SliderPicker)
  (r/render [app] (sel1 :#app)))


(defn ^:export init [& args]
  (reload))


(defn ^:export -main [& args]
  (init args))
