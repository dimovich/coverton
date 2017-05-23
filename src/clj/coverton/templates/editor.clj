(ns coverton.templates.editor
  (:require [hiccup.page :refer [html5 include-css include-js]]))

(defn editor []
  (html5
   {:lang "en"}
   [:head
    [:title "Coverton Editor"]
    (include-css "assets/css/style.css")]
   [:body
    [:div.wrap
     [:div.app]]
    (include-js "main.js")
    [:script "coverton.core.init();"]]))




;; http://petercollingridge.appspot.com/svg-optimiser/
;; borders:   https://jsfiddle.net/yyp67pbg/
;; http://www.cssstickyfooter.com/using-sticky-footer-code.html
;; http://alistapart.com/article/holygrail

