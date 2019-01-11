(ns coverton.core
  (:require [taoensso.timbre :refer [info]]
            [roll.core :as roll]
            [coverton.db :as db]
            [coverton.db.util :as db-util]
            [coverton.handler :as handler]))


(defn init [& args]
  ;; start webserver, websocket, repl and others
  (roll/init "conf/config.edn")

  (db/init)
  (db-util/import-db)
  
  (info "[DONE]"))


(defn -main [& args]
  ;;(timbre/set-level! :error)
  (init))

