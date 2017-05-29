(ns coverton.templates.devcards
  (:require [hiccup.page :refer [html5 include-css include-js]]))


(defn devcards []
  (html5
   {:lang "en"}
   [:head
    [:title "Coverton Editor - Devcards"]
    (include-css "assets/css/style.css")]
   [:body
    [:div.wrap
     [:div.app]]
    [:span#span-measure]
    (include-js "main.js")
    [:script "coverton.devcards.init();"]]))


