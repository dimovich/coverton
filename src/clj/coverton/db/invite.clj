(ns coverton.db.invite
  (:require [coverton.db.core :as db]
            [buddy.core.nonce  :as nonce]
            [buddy.core.codecs :as codecs]))



(defn gen-code [length]
  (-> (nonce/random-bytes length)
      (codecs/bytes->hex)))



(defn gen-invites [n & {length :length :or {length 3}}]
  (->> #(gen-code length)
       repeatedly
       (take n)
       (mapcat #(-> [{:invite/code %
                      :invite/status :new}]))
       db/transact))



(defn get-invite-code []
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



(defn expire-invite-code [code]
  (db/transact [{:invite/code code
                 :invite/status :expired}]))




(defn get-all-invites []
  (->> '[:find (pull ?e [*])
         :where [?e :invite/code]]
       db/query-db
       (apply concat)
       (map #(-> % (dissoc :db/id)))))




(defn retract-expired-invites []
  (->> (get-all-invites)
       (filter #(= :expired (:invite/status %)))
       (map :db/id)
       (map db/retract-entity)))




(defn request-invite [email story]
  (let [url "https://coverton.co/approve?email=dimovich@gmail.com&secret=5fgkjfglskgjflgj"
        secret (gen-code)]))
