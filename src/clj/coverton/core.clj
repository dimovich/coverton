(ns coverton.core
  (:require [clojure.java.io :as io]

            [ring.middleware.resource        :refer [wrap-resource]]
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
            [coverton.util :refer [ok bad-request random-uuid]]
            [coverton.templates.index :refer [index static-promo]]
            [coverton.db.schema :refer [cover->db]]

            [coverton.db.core   :as db]
            [coverton.db.util   :as db-util]
            [coverton.db.covers :as db-covers]
            [coverton.db.users  :as db-users]
            [coverton.repl :as repl])
  
  (:gen-class))


(defonce state (atom nil))



(defn handle-save-cover [{{:keys [cover/id] :as cover} :params :as request}]
  (if (authenticated? request)
    (let [author (:username (:identity request))
          id     (or id (random-uuid))
          cover (merge cover {:cover/id id
                              :cover/author author})
          cover (assoc cover :cover/data (.array (fress/write cover)))]

      (info "adding: " cover)

      (-> cover
          (select-keys cover->db)
          db/transact)
    
      (ok {:cover/id id}))
    
    (throw-unauthorized)))



(defn get-cover [{{id :id} :params}]
  (let [_     (info "getting" id)
        cover (db-covers/get-cover id)]
    (if (:cover/data cover)
      (ok (fress/read (:cover/data cover)))
      (not-found (str id)))))



(defn handle-get-covers
  [{{:keys [tags size skip] :as params} :params :as request}]
  (ok {:covers
       (->> (cond
              (not (empty? tags)) (db-covers/get-covers {:tags tags})
              :default (db-covers/get-all-covers))
            (map :cover/data))}))



(defn handle-upload-file
  [{{{:keys [filename tempfile]} "file"} :params :as request}]
  
  (if (authenticated? request)

    (let [id (random-uuid)
          path (str "uploads/" id ".jpg")]
      (io/copy tempfile (io/file path))
      (ok {:image-url path}))
    
    (throw-unauthorized)))



(defn handle-export-db [request]
  (if (authenticated? request)
    (when (= "dimovich" (:username (:identity request)))
      (db-util/export-db)
      (ok {:message "all good"}))
    (throw-unauthorized)))




(defroutes handler
  (GET  "/"           [] (static-promo))
  (GET  "/index"      [] (index))

  (POST "/save-cover" [] handle-save-cover)
  (POST "/get-cover"  [] get-cover)

  (POST "/get-covers" [] handle-get-covers)
  (POST "/login"      [] login)

  (POST "/upload-file" [] handle-upload-file)

  (GET "/export-db" [] handle-export-db)
  
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




(defn destroy []
  (info "shutting down...")

  (repl/stop)

  (when-let [server (:server @state)]
    (server :timeout 100))

  (info "bye!"))



(defn init []
  (.addShutdownHook (Runtime/getRuntime) (Thread. destroy))
  
  (db/init)
  (db-util/import-db)
  (repl/start))



;; TODO: option to initialize db
(defn -main [& args]
  (->> (server/run-server app {:port 5000})
       (swap! state assoc :server))
  
  (info "started server")

  (init))





;; use spec for schema
;; use component to init



#_(rename-keys cover->db-map)
#_(update-in [:cover/marks]
             #(map (fn [m] (rename-keys m mark->db-map)) %))



;; https://zaiste.net/posts/file_uploads_in_a_clojure_web_application_using_compojure/
