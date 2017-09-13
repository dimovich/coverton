(ns coverton.ajax.events
  (:require [re-frame.core :as rf   :refer [reg-event-db reg-event-fx subscribe path trim-v]]
            [ajax.core     :as ajax :refer [to-interceptor]]
            [day8.re-frame.http-fx]
            [coverton.ajax.subs :as sub]
            [taoensso.timbre :refer-macros [info]]))



(def ajax-interceptors [(path :ajax) trim-v])



(defn inject-token [request]
  (if-let [token @(subscribe [::sub/token])]
    (-> request
        (update :headers
                #(merge % {"Authorization" (str "Token " token)})))
    request))

(def token-ajax-interceptor
  (to-interceptor {:name "token interceptor"
                   :request inject-token}))





(reg-event-fx
 ::request
 ajax-interceptors
 (fn [_ [m]]
   {:http-xhrio (-> {:method          :get
                     :on-success      [::good-response]
                     :on-failure      [::bad-response]
                     ;; :format          (ajax/transit-request-format)
                     :response-format (ajax/transit-response-format)}
                    
                    (merge m))}))


(reg-event-fx
 ::request-auth
 ajax-interceptors
 (fn [_ [m]]
   {:dispatch
    [::request (-> {:interceptors [token-ajax-interceptor]}
                   (merge m))]}))


(reg-event-fx
 ::request-raw-auth
 ajax-interceptors
 (fn [_ [m]]
   {:dispatch
    [::request-auth (-> {:response-format (ajax/raw-response-format)}
                        (merge m))]}))


(reg-event-db
 ::set-token
 ajax-interceptors
 (fn [db [{:keys [token]}]]
   (info "login success: " token)
   (assoc db :token token)))


(reg-event-db
 ::remove-token
 ajax-interceptors
 (fn [db _]
   (info "logout success.")
   (dissoc db :token)))



;;fixme: we don't change the db, can we ignore the return of the fn?
(reg-event-db
 ::good-response
 ajax-interceptors
 (fn
   [db [response]]
   (info "ajax success: " response)
   db))


(reg-event-db
 ::bad-response
 ajax-interceptors
 (fn [db [response]]
   (info "ajax error: " response)
   db))


