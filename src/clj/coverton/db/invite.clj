(ns coverton.db.invite
  (:require [coverton.db.core :as db]
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
       (map #(-> % (dissoc :db/id)))))



(def message-body "Hello\nSomeone wants to join Coverton.\n\nEmail: %s\n\nStory:\n%s\n\nApprove:\n%s")


;; TODO: escape story string
(defn request-invite [{:keys [email story]}]
  ;; check if already present
  
  (let [secret (gen-code)
        entity {:invite/email  email
                :invite/story  story
                :invite/secret secret
                :invite/status :sent}
        url (-> "https://coverton.co/approve?email=%s&secret=%s"
                (format email secret))
        msg (-> message-body
                (format email story url))]
    ;;send email
    
    ;;check if sent and change status
    
    ;;add to db
    (db/transact entity)))



(defn approve-invite [{:keys [email secret] :as args}]
  (if-let [entity (-> '[:find (pull ?e [*])
                          :in $ ?email ?secret
                          :where
                          [?e :invite/email ?email]
                          [?e :invite/secret ?secret]
                          [?e :invite/status :sent]]
                        (db/query-db email secret)
                        ffirst)]
    (let []
      ;; update entity to :approved
      ;; send register url with secret
      )
    (info "Could not find entity for approval: " args)))









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
