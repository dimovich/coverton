(ns coverton.fonts
  (:require clojure.string))


(def font-names '("Playfair Display"
                  "Abril Fatface"
                  "Alfa Slab One"
                  "Paytone One"
                  "Arbutus Slab"
                  "Aclonica"
                  "Six Caps"
                  "Yeseva One"
                  "Gravitas One"
                  "Vampiro One"))


(def default-font {:font-size 0.13 ;; % of height
                   :font-family "GothaPro"
                   :color "orange"})

(defn for-css []
  (->> font-names
       (map #(clojure.string/replace % #" " "+"))
       (clojure.string/join "|")))

