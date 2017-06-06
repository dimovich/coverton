(ns coverton.core
  (:require [compojure.core     :refer [defroutes GET POST]]
            [compojure.route    :refer [not-found files resources]]
            [compojure.handler  :refer [site]]
            [ring.util.response :refer [file-response]]
            [coverton.templates.editor   :refer [editor]]
            [coverton.templates.devcards :refer [devcards]]
            [coverton.templates.index    :refer [index]]            
            [org.httpkit.server :as server])
  (:gen-class))



(defroutes handler
  (GET "/" [] (index))
  (GET "/devcards" [] (devcards))
  (GET "/editor" [] (editor))
  (files "/" {:root "."})     ;; to serve static resources
  (resources "/" {:root "."}) ;; to serve anything else
  (not-found "Page Not Found"))    ;; page not found


(def app
  (-> handler
      (site)))


(defn -main [& args]
  (server/run-server app {:port 3000}))
