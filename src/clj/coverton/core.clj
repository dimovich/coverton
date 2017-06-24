(ns coverton.core
  (:require [compojure.core     :refer [defroutes GET POST]]
            [compojure.route    :refer [not-found files resources]]
            [compojure.handler  :refer [site]]
            [ring.util.response :refer [file-response]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.not-modified :refer [wrap-not-modified]]
            [coverton.templates.devcards :refer [devcards]]
            [coverton.templates.index    :refer [index static-promo]]
            [org.httpkit.server :as server]
            [namen.core :as namen]
            [cheshire.core :as json])
  (:gen-class))



(defroutes handler
  (GET "/"         [] (static-promo))
  (GET "/devcards" [] (devcards))
  (GET "/wordizer" [] (namen/frontend))
  (GET "/generate" xs (json/generate-string
                       (namen/generate (-> xs :params :words vals))))
  (GET "/index"    [] (index))
  (files     "/" {:root "."})   ;; to serve static resources
  (resources "/" {:root "."})   ;; to serve anything else
  (not-found "Page Not Found")) ;; page not found


(def app
  (-> handler
      (wrap-resource "public")
      (wrap-content-type)
      (wrap-not-modified)
      (site)))


(defn -main [& args]
  (server/run-server app {:port 80})
  (println "started server"))
