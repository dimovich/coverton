(ns coverton.core
  (:require [taoensso.timbre :as timbre :refer [info]]
            [coverton.system :refer [init]])
  
  (:gen-class))


(defn -main [& args]
  (init {:path "config.edn"}))





;; https://zaiste.net/posts/file_uploads_in_a_clojure_web_application_using_compojure/
