(ns coverton.index.subs
  (:require [re-frame.core :refer [reg-sub]]))


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
