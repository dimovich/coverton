(ns coverton.templates.index
  (:require [hiccup.page :refer [html5 include-css include-js]]))



(defn index []
  (html5
   {:lang "en"}
   [:head
    [:title "Coverton"]
    (include-css "assets/css/style.css")
    [:link {:rel "shortcut icon"
            :href "assets/img/f.ico"
            :type "image/x-icon"}]]
   [:body
    [:div.wrap
     [:div#app]]
    (include-js "coverton.js")
    [:script "coverton.core.init();"]]))



(defn index-register []
  (html5
   {:lang "en"}
   [:head
    [:title "Coverton Editor"]
    (include-css "assets/css/style.css")
    ;;(include-css "https://cdnjs.cloudflare.com/ajax/libs/semantic-ui/2.2.10/semantic.min.css")
    [:link {:rel "shortcut icon"
            :href "assets/img/f.ico"
            :type "image/x-icon"}]]
   [:body
    [:div.wrap
     [:div#app]]
    [:span#span-measure]
    (include-js "coverton.js")
    [:script "coverton.core.init(\"register\");"]]))




(defn static-promo []
  (html5
   {:lang "en"}
   [:head
    [:title "Coverton"]
    [:meta {:charset "utf-8"}]
    (include-css "assets/css/style.css")
    [:link {:rel "shortcut icon"
            :href "assets/img/f.ico"
            :type "image/x-icon"}]]
   [:body
    [:div {:class "content vcenter"}
     [:div.motto
      [:img.logo {:src "assets/svg/logo.svg"}]
      [:p.text
       "a publishing platform for cover makers"
       [:br]
       "is coming soon."]]]]))


