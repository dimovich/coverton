(ns coverton.util
  (:require [reagent.core :as r]
            [dommy.core :refer [sel1]]
            [taoensso.timbre :refer-macros [info]]))



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




(defn form-data [id]
  (info "getting form data")
  (when-let [file (some-> (sel1 id)
                          .-files
                          (aget 0))]
    (info "got" file)
    (doto (js/FormData.)
      (.append "file" file))))
