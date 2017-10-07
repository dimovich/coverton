(ns coverton.mail
  (:require [postal.core :as postal]))


(defn send-mail [opts]
  (postal/send-message {:host "smtp.gmail.com"
                        :user "coverton.mailer@gmail.com"
                        :pass "C0ver.Ton"
                        :ssl   true}
                       (-> {:from "coverton.mailer@gmail.com"}
                           (merge opts))))
