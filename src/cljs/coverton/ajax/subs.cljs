(ns coverton.ajax.subs
  (:require [re-frame.core :refer [reg-sub]]))


(reg-sub
 ::ajax
 (fn [db _]
   (:ajax db)))

(reg-sub
 ::token
 :<- [::ajax]
 (fn [db _]
   (:token db)))

