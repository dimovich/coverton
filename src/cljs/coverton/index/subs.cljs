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
 ::authenticated?
 :<- [::ajax-sub/token]
 (fn [token _]
   (not (nil? token))))
