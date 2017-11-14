(ns coverton.fabric.defaults
  (:require [coverton.fonts :refer [default-font]]))


(def mark
  (clj->js {:fill (:color default-font) :cursorColor (:color default-font)
            :fontFamily (:font-family default-font) :fontSize 80}))

