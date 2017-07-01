(ns coverton.util)

(defn info [& args]
  (enable-console-print!)
  (apply println args))
