(ns coverton.core
  (:require [ring.middleware.resource     :refer [wrap-resource]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.not-modified :refer [wrap-not-modified]]
            [coverton.templates.devcards  :refer [devcards]]
            [ring.middleware.format       :refer [wrap-restful-format]]
            [coverton.templates.index     :refer [index static-promo]]
            [compojure.core     :refer [defroutes GET POST PUT]]
            [compojure.route    :refer [not-found files resources]]
            [compojure.handler  :refer [site]]
            [ring.util.response :refer [file-response]]
            [org.httpkit.server :as server]
            [taoensso.timbre    :as timbre :refer [info]]
            [cheshire.core      :as json]
            [namen.core         :as namen]
            [coverton.db.core   :as db]
            [clojure.pprint     :refer [pprint]]
            [clojure.set :refer [rename-keys]]
            [ring.util.response :refer [response]]
            [ring.middleware.transit :refer [wrap-transit-response]])
  
  (:gen-class))


(defonce state (atom nil))

(def magic-id #uuid "1d822372-983a-4012-adbc-569f412733fd")


(timbre/set-config!
 {:level :info
  :output-fn (fn [{:keys [timestamp_ level msg_]}]
               (str
                (second (clojure.string/split (force timestamp_) #" ")) " "
                ;;(clojure.string/upper-case (name level)) " "
                (force msg_)))
  :appenders {:println (timbre/println-appender {:stream :auto})}})



(def cover->db-map
  {:image-url :cover/image-url
   :tags      :cover/tags
   :size      :cover/size
   :marks     :cover/marks
   :cover-id  :cover/id})


(def mark->db-map
  {:mark-id     :mark/id
   :pos         :mark/pos
   :font-size   :mark/font-size
   :font-family :mark/font-family
   :text        :mark/text
   :color       :mark/color})



;; server should gen uuids
(defn save-cover [req]
  (let [cover (-> (get-in req [:params :cover])
                  (rename-keys cover->db-map)
                  (update-in [:cover/marks]
                             #(map (fn [m] (rename-keys m mark->db-map)) %)))
        cover-id (or (:cover/id cover) magic-id) ;;generate uuid
        cover    (assoc cover :cover/id cover-id)]
    (info (db/add-data cover))
    (response cover-id)))



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
      (wrap-restful-format {:formats [:transit-json]})
      (wrap-resource "public")
      (wrap-content-type)
      (wrap-not-modified)
      (wrap-transit-response)
      (site)))


;; boot runs this from another process... so no state in the end
(defn init []
  (swap! state assoc :conn (db/init))
  (info "state: " @state))


(defn -main [& args]
  (swap! state assoc :server (server/run-server app {:port 80}))
  (info "started server")
  (init))


;; uniform data schema
;; use spec for schema
;; use component to init

