(ns coverton.core
  (:require [ring.middleware.resource        :refer [wrap-resource]]
            [ring.middleware.content-type    :refer [wrap-content-type]]
            [ring.middleware.not-modified    :refer [wrap-not-modified]]
            [coverton.templates.devcards     :refer [devcards]]
            [ring.middleware.transit         :refer [wrap-transit-response wrap-transit-body]]
            [ring.util.response              :refer [response]]
            [coverton.templates.index        :refer [index static-promo]]
            [compojure.core     :refer [defroutes GET POST PUT]]
            [compojure.route    :refer [not-found files resources]]
            [compojure.handler  :refer [site]]
            [ring.util.response :refer [file-response]]
            [org.httpkit.server :as server]
            [taoensso.timbre    :as timbre :refer [info]]
            [cheshire.core      :as json]
            [namen.core         :as namen]
            [coverton.db.core   :as db]
            [coverton.db.schema :refer [mark->db-map cover->db-map magic-id]]
            [clojure.pprint     :refer [pprint]]
            [clojure.set :refer [rename-keys]]
            [clojure.data.fressian :as fress]
            [coverton.util])
  
  (:gen-class))


(defonce state (atom nil))


(defn save-cover [req]
  (let [cover (-> (get-in req [:body :cover])
                  (rename-keys cover->db-map)
                  (update-in [:cover/marks]
                             #(map (fn [m] (rename-keys m mark->db-map)) %)))
        cover-id (or (:cover/id cover) magic-id) ;;fixme
        cover    (assoc cover :cover/id cover-id)]
    (info (db/add-data {:cover/id cover-id
                        :cover/data (.array (fress/write cover))}))
    (response {:cover-id cover-id})))



(defroutes handler
  (GET "/"         [] (static-promo))
  (GET "/index"    [] (index))

  (POST "/save-cover" [] save-cover)
  
  (GET "/devcards" [] (devcards))
  
  (GET "/wordizer" [] (namen/frontend))
  (GET "/generate" xs (json/generate-string
                       (namen/generate (-> xs :params :words vals))))
  
  (files     "/" {:root "."})   ;; to serve static resources
  (resources "/" {:root "."})   ;; to serve anything else
  (not-found "Page Not Found")) ;; page not found


(def app
  (-> handler
      (wrap-transit-body)
      (wrap-transit-response)
      (wrap-resource "public")
      (wrap-content-type)
      (wrap-not-modified)
      (site)))


;; boot runs this from another process... so no state in the end
(defn init []
  (swap! state assoc :conn (db/init))
  (info "state: " @state))


(defn -main [& args]
  (swap! state assoc :server (server/run-server app {:port 80}))
  (info "started server")
  (init))


;; use spec for schema
;; use component to init

