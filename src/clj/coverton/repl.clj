(ns coverton.repl
  (:require [clojure.tools.nrepl.server :as nrepl-server]
            [cider.nrepl :refer [cider-nrepl-handler]]))


(defonce state (atom nil))


(defn start []
  (->> (nrepl-server/start-server :port 33000 :handler cider-nrepl-handler)
       (reset! state)))


(defn stop []
  (-> @state
      nrepl-server/stop-server))
