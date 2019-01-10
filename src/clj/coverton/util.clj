(ns coverton.util
  (:require [taoensso.timbre :as timbre :refer [info]]
            [clojure.java.io :as io]))


(timbre/set-config!
 {:level :info
  :output-fn (fn [{:keys [timestamp_ level msg_]}]
               (str
                (second (clojure.string/split (force timestamp_) #" ")) " "
                (force msg_)
                "\n\n"))
  :appenders {:println (timbre/println-appender {:stream :auto})}})



(defn ok [d]          {:status 200 :body d})
(defn bad-request [d] {:status 400 :body d})



;; check HN post for semantic uuids
(defn random-uuid []
  (java.util.UUID/randomUUID))

