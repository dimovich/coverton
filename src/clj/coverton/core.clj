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
            [taoensso.timbre :as timbre :refer [info]]
            [cheshire.core :as json]
            [namen.core :as namen]
            [coverton.db.core :as db])
  (:gen-class))


(def state (atom nil))


(timbre/set-config!
 {:level :info
  :output-fn (fn [{:keys [timestamp_ level msg_]}]
               (str
                (second (clojure.string/split (force timestamp_) #" ")) " "
                ;;(clojure.string/upper-case (name level)) " "
                (force msg_)))
  :appenders {:println (timbre/println-appender {:stream :auto})}})



(defroutes handler
  (GET "/"         [] (static-promo))
  (GET "/index"    [] (index))
  
  (GET "/devcards" [] (devcards))
  
  (GET "/wordizer" [] (namen/frontend))
  (GET "/generate" xs (json/generate-string
                       (namen/generate (-> xs :params :words vals))))
  
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
  (swap! state merge {:server (server/run-server app {:port 80})
                      :conn   (db/init)})
  (info "started server"))


;; transit
;; save button
;; author
