(ns coverton.db.invite
  (:require [coverton.db.core :as db]
            [buddy.core.nonce  :as nonce]
            [buddy.core.codecs :as codecs]))



(defn gen-invite-code [length]
  (-> (nonce/random-bytes length)
      (codecs/bytes->hex)))



(defn gen-invites [n & {length :length :or {length 3}}]
  (->> #(gen-invite-code length)
       repeatedly
       (take n)
       (mapcat #(-> [{:invite/code %
                      :invite/status :active}]))
       db/transact))



(defn get-invite-code []
  (let [code (-> '[:find (sample 1 ?code)
                   :where
                   [?e :invite/code   ?code]
                   [?e :invite/status :active]]
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




(defn get-invite-codes []
  (->> (db/query-db '[:find (pull ?e [*])
                      :where
                      [?e :invite/code ?code]])
       (apply concat)
       vec))




(defn retract-expired-invites []
  (->> (get-invite-codes)
       (filter #(= :expired (:invite/status %)))
       (map :db/id)
       (map db/retract-entity)))

