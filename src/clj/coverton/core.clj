(ns coverton.core
  (:require [compojure.core :refer [defroutes GET POST]]
            [compojure.route :refer [not-found files resources]]
            [compojure.handler :refer [site]]
            [coverton.templates.editor :refer [editor]]
            [coverton.templates.devcards :refer [devcards]]))



(defroutes handler
  (GET "/" [] (devcards))
  (GET "/editor" [] (editor))
  (files "/" {:root "target"})     ;; to serve static resources
  (resources "/" {:root "target"}) ;; to serve anything else
  (not-found "Page Not Found"))    ;; page not found


(def app
  (-> handler
      (site)))

