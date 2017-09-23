(ns coverton.index.subs
  (:require [re-frame.core :refer [reg-sub]]
            [coverton.ajax.subs :as ajax-sub]))


(reg-sub
 ::db
 (fn [db _]
   db))


(reg-sub
 ::index
 (fn [db _]
   (:index db)))


(reg-sub
 ::page
 :<- [::index]
 (fn [db _]
   (:page db)))


(reg-sub
 ::user
 :<- [::index]
 (fn [db _]
   (:user db)))


(reg-sub
 ::authenticated?
 :<- [::ajax-sub/token]
 (fn [token _]
   (not (nil? token))))


(reg-sub
 ::covers
 :<- [::index]
 (fn [db _]
   (:covers db)))


(reg-sub
 ::search-tags
 :<- [::index]
 (fn [db _]
   (:search-tags db)))
