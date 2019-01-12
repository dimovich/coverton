(ns coverton.handler
  (:require [ring.middleware.format          :refer [wrap-restful-format]]
            [ring.util.response              :refer [response file-response redirect not-found content-type]]

            [reitit.ring.middleware.multipart :as multipart]
            
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
            [coverton.schema :refer [cover->db]]

            [coverton.db.core   :as db]
            [coverton.db.util   :as db-util]
            [coverton.db.covers :as db-covers]
            [coverton.db.users  :as db-users]
            [coverton.db.invite :as invite])
  
  (:import [nginx.clojure NginxRequest]))



(defn handle-save-cover
  [{{:keys [cover/id] :as cover} :body-params :as request}]
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
  [{{:keys [tags size skip] :as params} :body-params :as request}]
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



(defn handle-confirm-invite
  [{{email "email" secret "secret"} :params}]
  (invite/confirm-invite {:email email
                          :secret secret})
  (html5
   [:body [:h1 "Success!"]]))



(defn handle-login [req]
  (login req))



(defn handle-register
  [{{email "email" secret "secret"} :params}]
  (html5
   [:body [:h1 "Under construction."]]))

(defonce req-trap (atom nil))

(defn handle-get-index [req]
  (ok (index)))


(def routes
  [["/" {:name :index
         :get handle-get-index}]

   ["/save-cover" {:name :save-cover
                   :post handle-save-cover}]

   ["/get-covers" {:name :get-covers
                   :post handle-get-covers}]

   ["/register" {:name :register
                 :get handle-register}]

   ["/login" {:name :login
              :post handle-login}]

   ["/request-invite" {:name :request-invite
                       :post handle-request-invite}]

   ["/confirm-invite" {:name :confirm-invite
                       :get handle-confirm-invite}]

   ["/upload-file" {:name :upload-file
                    :post handle-upload-files}]])




(defn wrap-info-request [handler]
  (fn [request]
    (info "request: " request)
    (handler request)))


(defn wrap-info-response [handler]
  (fn [request]
    (let [response (handler request)]
      (info "response: " response)
      response)))



(def middleware
  [[wrap-authorization auth-backend]
   [wrap-authentication auth-backend]
   [multipart/multipart-middleware]
   ;;[wrap-restful-format {:formats [:transit-json]}]
   ])
