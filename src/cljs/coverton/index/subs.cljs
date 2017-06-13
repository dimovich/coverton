(ns coverton.index.subs
  (:require [re-frame.core :refer [reg-sub]]))


(reg-sub
 ::index
 (fn [db _]
   (:index db)))


(reg-sub
 ::active-panel
 :<- [::index]
 (fn [index _]
   (:active-panel index)))
