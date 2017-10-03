(ns coverton.mail
  (:require [postal.core :refer [send-message]]))


(send-message {:host "smtp.gmail.com"})

(send-message {:from "coverton@coverton.co"
               :to "dimovich@gmail.com"
               :subject "Hi!"
               :body "Test."})
