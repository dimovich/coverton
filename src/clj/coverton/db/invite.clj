(ns coverton.db.invite
  (:require [coverton.db.core :as db]
            [coverton.mail    :as mail]
            [buddy.core.nonce  :as nonce]
            [buddy.core.codecs :as codecs]
            [taoensso.timbre :as timbre :refer [info]]))



(defn gen-code
  ([] (gen-code 3))
  ([length]
   (-> (nonce/random-bytes length)
       (codecs/bytes->hex))))



(defn get-all-invites []
  (->> '[:find (pull ?e [*])
         :where [?e :invite/email]]
       db/query-db
       (apply concat)
       (map #(dissoc % :db/id))))


(def message-body "Hello\nSomeone wants to join Coverton.\n\nEmail: %s\n\nStory:\n%s\n\nConfirm:\n%s")

;;(def hostname "http://localhost:5000/")
(def hostname "https://coverton.co")


;; TODO: escape story string
(defn request-invite [{:keys [email story]}]
  ;;todo: check if already present
  (let [secret (gen-code)
        url (-> (str hostname "/confirm-invite?email=%s&secret=%s")
                (format email secret))
        msg (-> message-body
                (format email story url))
        subject (-> "Coverton - Invitation Request from %s"
                    (format email))]

    ;;add to db
    (-> {:invite/email  email
         :invite/story  story
         :invite/secret secret
         :invite/status :new}
        db/transact)
    
    ;;send request for approval
    ;;todo: try a few times
    (try
      (mail/send-mail {:to "dimovich@gmail.com"
                       :subject subject
                       :body msg})
      ;;update status
      (-> {:invite/email  email
           :invite/status :request}
          db/transact)
      
      (catch Exception e (info (.getMessage e))))))




(defn check-invite [email secret]
  (-> '[:find (pull ?e [*])
        :in $ ?email ?secret
        :where
        [?e :invite/email ?email]
        [?e :invite/secret ?secret]
        [?e :invite/status :request]]
      (db/query-db email secret)
      ffirst))



(defn confirm-invite [{:keys [email secret] :as args}]
  (if (check-invite email secret)
    (let [url (-> (str hostname "/register?email=%s&secret=%s")
                  (format email secret))
          msg (-> "Follow this link to register: %s"
                  (format url))]

      ;; update entity to :confirmed
      (-> {:invite/email  email
           :invite/status :confirmed}
          db/transact)

      ;; send registration
      (try
        (mail/send-mail {:to email
                         :subject "Coverton Invitation"
                         :body msg})
        ;;update status
        (-> {:invite/email  email
             :invite/status :sent}
            db/transact)
      
        (catch Exception e (info (.getMessage e)))))
    
    (info "Could not check entity for approval: " args)))









#_(defn gen-invites [n & {length :length :or {length 3}}]
    (->> #(gen-code length)
         repeatedly
         (take n)
         (mapcat #(-> [{:invite/code %
                        :invite/status :new}]))
         db/transact))



#_(defn get-invite-code []
    (let [code (-> '[:find (sample 1 ?code) ;;first??
                     :where
                     [?e :invite/code   ?code]
                     [?e :invite/status :new]]
                   db/query-db
                   ffirst
                   first)]
      ;; change status
      (db/transact [{:invite/code code
                     :invite/status :sent}])
      code))




#_(defn retract-expired-invites []
    (->> (get-all-invites)
         (filter #(= :expired (:invite/status %)))
         (map :db/id)
         (map db/retract-entity)))
