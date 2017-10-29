(ns coverton.core
  (:require [taoensso.timbre :as timbre :refer [info]]
            [coverton.system :refer [init]])
  
  (:gen-class))


(defn -main [& args]
  ;;(timbre/set-level! :error)
  (init {:path "config.edn"}))


