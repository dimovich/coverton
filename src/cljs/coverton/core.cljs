(ns coverton.core
  (:require [reagent.core :as r]
            [dommy.core :as d :refer-macros [sel1]]
            [coverton.index.views  :as index]))


(defn app []
  [index/index])


(defn ^:export reload []
  (r/render [app] (sel1 :#app)))


(defn ^:export init []
  (reload))


(defn ^:export -main [& args]
  (init))
