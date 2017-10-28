(ns coverton.handler
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

            [hiccup.page :refer [html5]]

            [buddy.sign.jwt :as jwt]
            [buddy.auth     :refer [authenticated? throw-unauthorized]]
            [buddy.auth.middleware :refer [wrap-authentication
                                           wrap-authorization]]
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
            [coverton.db.invite :as invite]))



(defn handle-save-cover
  [{{:keys [cover/id] :as cover} :params :as request}]
  (if (authenticated? request)
    (let [author (:email (:identity request))
          id     (or id (random-uuid))
          cover (merge cover {:cover/id id
                              :cover/author author})
          cover (assoc cover :cover/data
                       (.array (fress/write cover)))]

      (info "adding cover:" cover)
      (-> cover
          (select-keys cover->db)
          db/transact)
    
      (ok {:cover/id id}))
    
    (throw-unauthorized)))



(defn get-cover
  [{{id :id} :params}]
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



(defn handle-upload-files
  [{params :params :as request}]
  
  (if (authenticated? request)
    (do
      (info "uploading files...")
      (->> params
           (map (fn [[k file]]
                  (let [id (random-uuid)
                        ext (or (re-find #"[.].*$" (:filename file)) ".jpg")
                        path (str "uploads/" id ext)
                        resp {(keyword k) path}]
                    (io/copy (:tempfile file) (io/file path))
                    (info resp)
                    resp)))
           (apply merge)
           ok))
    
    (throw-unauthorized)))



(defn handle-request-invite
  [{{:keys [email story] :as params} :params}]
  (info "requesting invite: " params)
  
  (invite/request-invite {:email email
                          :story story})
  
  (ok {:request-invite-sent true}))



(defn handle-approve-invite
  [{{email "email" secret "secret"} :params}]
  (invite/approve-invite {:email email
                          :secret secret})
  (html5
   [:body [:h1 "Approved."]]))



(defn handle-login
  [req]
  (login req))



(defn handle-register
  [{{email "email" secret "secret"} :params}]
  (html5
   [:body [:h1 "Under construction."]]))



(defroutes handler
  (GET  "/"           [] (static-promo))
  (GET  "/index"      [] (index))

  (POST "/save-cover" [] handle-save-cover)
  ;;(POST "/get-cover"  [] get-cover)

  (POST "/get-covers" [] handle-get-covers)

  (GET  "/register"   [] handle-register)
  (POST "/login"      [] handle-login)

  (POST "/request-invite" [] handle-request-invite)
  (GET  "/approve-invite" [] handle-approve-invite)


  (POST "/upload-file" [] handle-upload-files)
  
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
    ;;(wrap-info-request $)
    (wrap-authorization  $ auth-backend)
    (wrap-authentication $ auth-backend)
    (wrap-restful-format $ {:formats [:transit-json]})
    (wrap-params         $)
    (wrap-multipart-params $)
    (wrap-resource       $ "public")
    (wrap-content-type   $)
    ;;(wrap-info-response  $)
    ))

