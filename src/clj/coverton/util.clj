(ns coverton.util
  (:require [taoensso.timbre :as timbre]))


(timbre/set-config!
 {:level :info
  :output-fn (fn [{:keys [timestamp_ level msg_]}]
               (str
                (second (clojure.string/split (force timestamp_) #" ")) " "
                (force msg_)))
  :appenders {:println (timbre/println-appender {:stream :auto})}})
