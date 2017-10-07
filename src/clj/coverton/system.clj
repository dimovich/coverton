(ns coverton.system
  (:require [coverton.repl      :as repl]
            [coverton.handler   :as handler]
            [integrant.core     :as ig]
            [org.httpkit.server :as server]
            [coverton.db.core   :as db]
            [coverton.db.util   :as db-util]
            [taoensso.timbre    :as timbre :refer [info]]))


(defmethod ig/init-key :adapter/server [_ opts]
  (info "starting server with: " opts)
  (server/run-server handler/app opts))


(defmethod ig/halt-key! :adapter/server [_ server]
  (when server
    (server)))



(defmethod ig/init-key :repl/repl [_ opts]
  (repl/start opts))

(defmethod ig/halt-key! :repl/repl [_ server]
  (some-> server
          repl/stop))



(defonce state (atom nil))


(defn destroy []
  (info "shutting down...")
  (some-> (:system @state)
          ig/halt!))


(defn init [{path :path}]
  (let [config (ig/read-string (slurp path))]
    (.addShutdownHook (Runtime/getRuntime) (Thread. destroy))

    (swap! state assoc :config config)
    
    (->> config
         ig/init
         (swap! state assoc :system))
  
    (db/init)
    (db-util/import-db)))



(defn restart [k]
  (ig/halt! (:system @state) [k])
  (->> (ig/init (:config @state) [k])
       (swap! state update :system merge)))

