(ns coverton.auth
  (:require [buddy.core.nonce  :as nonce]
            [buddy.core.codecs :as codecs]
            [buddy.sign.jwt :as jwt]
            [buddy.hashers  :as hashers]
            [buddy.auth     :refer [authenticated? throw-unauthorized]]
            [buddy.auth.backends.token :refer [jws-backend]]
            [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
            [clj-time.core      :as time]
            [coverton.util      :refer [ok bad-request]]
            [taoensso.timbre    :refer [info]]
            [coverton.db.users   :as db-users]))



(defonce secret (-> (nonce/random-bytes 32)
                    (codecs/bytes->hex)))


(defn login [{{:keys [email password]} :params :as request}]

  (let [valid? (some->> email
                        db-users/get-user-by-email
                        :user/password
                        (hashers/check password))]
    (if valid?
      (let [claims {:email email
                    :exp  (time/plus (time/now) (time/seconds 3600))}
            token (jwt/sign claims secret {:alg :hs512})]

        (ok {:token token}))
      
      (bad-request {:message "wrong auth data"}))))




(defn unauthorized-handler
  [request metadata]
  (cond
    (authenticated? request)
    (-> (ok)
        (assoc :status 403))

    :else
    (bad-request {:message "unauthorized"})))



(def auth-backend
  (jws-backend {:unauthorized-handler unauthorized-handler
                :secret secret :options {:alg :hs512}}))



;; (hashers/derive pass)
;; (hashers/check pass derived)

