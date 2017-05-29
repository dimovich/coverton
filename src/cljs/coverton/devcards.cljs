(ns coverton.devcards
  (:require [devcards.core :as dc :include-macros true])
)

(defn ^:export init []
  (dc/start-devcard-ui!))
