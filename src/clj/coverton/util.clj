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



(defn random-uuid []
  (java.util.UUID/randomUUID))



(defmacro when-read [[name fname] & body]
  `(let [file# (io/file ~fname)]
     (if (.exists file#)
       (with-open [rdr# (io/reader file#)]
         (let [~name (slurp rdr#)]
           ~@body))
       (info ~fname " doesn't exist."))))


