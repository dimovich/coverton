(ns coverton.core
  (:require [ring.middleware.resource        :refer [wrap-resource]]
            [ring.middleware.content-type    :refer [wrap-content-type]]
            [ring.middleware.not-modified    :refer [wrap-not-modified]]
            [ring.middleware.format          :refer [wrap-restful-format]]
            [ring.util.response              :refer [response file-response redirect not-found content-type]]
            [ring.middleware.session         :refer [wrap-session]]
            [ring.middleware.params          :refer [wrap-params]]
            [ring.middleware.multipart-params :refer [wrap-multipart-params]]

            [compojure.core     :refer [defroutes GET POST]]
            [compojure.route    :refer [files resources]]
            [compojure.response :refer [render]]

            [buddy.sign.jwt :as jwt]
            [buddy.auth     :refer [authenticated? throw-unauthorized]]
            [buddy.auth.middleware :refer [wrap-authentication
                                           wrap-authorization]]

            [org.httpkit.server    :as server]
            [taoensso.timbre       :as timbre :refer [info]]
            [clojure.data.fressian :as fress]
            [clojure.java.io       :as io]
            [clojure.set :refer [rename-keys]]
            
            [coverton.auth :refer [auth-backend login]]
            [coverton.util :refer [ok bad-request]]
            [coverton.templates.index        :refer [index static-promo]]
            [coverton.db.core   :as db]
            [coverton.db.schema :refer [mark->db-map cover->db-map magic-id]])
  
  (:gen-class))


(defonce state (atom nil))


(defn save-cover [{{:keys [cover-id] :as cover} :params :as request}]
  (if (authenticated? request)
    (let [author (:username (:identity request))
          cover-id (or cover-id magic-id) ;;fixme: generate new
          cover    (-> cover
                       (assoc :cover-id cover-id))]

      (info "adding data...")
      
      (db/add-data {:cover/id cover-id
                    :cover/author author
                    :cover/data (.array (fress/write cover))})
    
      (ok {:cover-id cover-id}))
    
    (throw-unauthorized)))



(defn get-cover [{{id :id} :params}]
  (let [_    (info "getting" id)
        cover    (db/get-cover id)]
    (if (:cover/data cover)
      (ok (fress/read (:cover/data cover)))
      (not-found (str id)))))




(def covers-sample {:cover1 {:some :idata}
                    :cover2 {:some :odata}})

(defn get-covers [{{:keys [type size skip]} :params :as request}]
  (ok covers-sample))


(defn upload-file [req]
  (info (keys req)))


(defroutes handler
  (GET  "/"           [] (static-promo))
  (GET  "/index"      [] (index))

  (POST "/save-cover" [] save-cover)
  (POST "/get-cover"  [] get-cover)

  (POST "/get-covers" [] get-covers)
  (POST "/login"      [] login)

  (POST "/upload-file" [file] (upload-file file))
  
  (files     "/" {:root "."}) ;; to serve static resources
  (resources "/" {:root "."}) ;; to serve anything else
  (compojure.route/not-found "Page Not Found")) ;; page not found



(defn wrap-info-request [handler]
  (fn [request]
    (info "request: " request)
    (handler request)))


(defn wrap-info-response [handler]
  (fn [request]
    (let [response (handler request)]
      (info "response: " response)
      response)))



(def app
  (as-> handler $
    (wrap-info-request   $)
    (wrap-authorization  $ auth-backend)
    (wrap-authentication $ auth-backend)
    (wrap-restful-format $ {:formats [:transit-json]})
    (wrap-params         $)
    (wrap-multipart-params $)
    (wrap-resource       $ "public")
    (wrap-content-type   $)
    ;;(wrap-info-response  $)
    ))




;; boot runs this from another process... so no state in the end
(defn init []
  (db/init))


;; TODO: option to initialize db
(defn -main [& args]
  (swap! state assoc :server (server/run-server app {:port 80}))
  (info "started server")
  (init))





;; use spec for schema
;; use component to init



#_(rename-keys cover->db-map)
#_(update-in [:cover/marks]
             #(map (fn [m] (rename-keys m mark->db-map)) %))



;; https://zaiste.net/posts/file_uploads_in_a_clojure_web_application_using_compojure/
