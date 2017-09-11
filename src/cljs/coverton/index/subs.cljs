(ns coverton.index.subs
  (:require [re-frame.core :refer [reg-sub]]
            [coverton.ajax.subs :as ajax-sub]))


(reg-sub
 ::index
 (fn [db _]
   (:index db)))


(reg-sub
 ::panel-stack
 :<- [::index]
 (fn [db _]
   (:panel-stack db)))


(reg-sub
 ::active-panel
 :<- [::panel-stack]
 (fn [panels _]
   (first panels)))


(reg-sub
 ::authenticated?
 :<- [::ajax-sub/token]
 (fn [token _]
   (not (nil? token))))
