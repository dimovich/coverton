(ns coverton.templates.index
  (:require [hiccup.page :refer [html5 include-css include-js]]))



(defn index []
  (html5
   {:lang "en"}
   [:head
    [:title "Coverton"]
    (include-css "/css/style.css")
    [:link {:rel "shortcut icon"
            :href "/img/f.ico"
            :type "image/x-icon"}]]
   [:body
    [:div.wrap
     [:div#app]]
    (include-js "/js/coverton.js")
    [:script "coverton.core.init();"]]))



(defn static-promo []
  (html5
   {:lang "en"}
   [:head
    [:title "Coverton"]
    [:meta {:charset "utf-8"}]
    (include-css "/css/style.css")
    [:link {:rel "shortcut icon"
            :href "/img/f.ico"
            :type "image/x-icon"}]]
   [:body
    [:div {:class "content vcenter"}
     [:div.motto
      [:img.logo {:src "/svg/logo.svg"}]
      [:p.text
       "a publishing platform for cover makers"
       [:br]
       "is coming soon."]]]]))


