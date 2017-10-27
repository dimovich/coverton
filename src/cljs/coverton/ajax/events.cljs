(ns coverton.ajax.events
  (:require [re-frame.core :as rf   :refer [reg-event-db reg-event-fx reg-fx
                                            subscribe path trim-v]]
            [ajax.core     :as ajax :refer [to-interceptor]]
            [day8.re-frame.http-fx]
            [coverton.ajax.subs :as sub]
            [taoensso.timbre :refer-macros [info]]))



(def ajax-interceptors [(path :ajax) trim-v])
(def token (subscribe [::sub/token]))



(defn inject-token [request]
  (if @token
    (-> request
        (update :headers
                #(merge % {"Authorization" (str "Token " @token)})))
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
                     :format          (ajax/transit-request-format)
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



(reg-fx
 ::good-response
 (fn [response]
   (info "ajax success: " response)))


(reg-fx
 ::bad-response
 (fn [response]
   (info "ajax error: " response)))


