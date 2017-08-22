(ns coverton.index.views
  (:require [reagent.core  :as r]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [coverton.index.events :as evt]
            [coverton.index.subs   :as sub]
            [coverton.ed.views     :as ed]
            [coverton.ed.subs      :as ed-sub]
            [coverton.components   :as cc]
            [coverton.util         :refer [info]]
            [coverton.db.schema    :refer [magic-id]]))

(enable-console-print!)



(defn test-fn []
  (let [_ (println "test-fn init")]
    (r/create-class
     {:display-name "test-fn"
      :component-did-mount
      (fn [this]
        (let [_ (println "test-fn mounted")]))
      :reagent-render
      (fn []
        (let [_ (println "test-fn render")]))})))



(defn index []
  (r/with-let [ ;;_            (dispatch-sync [::evt/initialize])
               active-panel (subscribe [::sub/active-panel])]

    (condp = @active-panel
      ;; Editor
      :ed [ed/editor {:cover {}}]

      ;; Index
      [:div.index
       [cc/Button {:on-click #(evt/push-panel :ed)}
        "Editor"]
       
       [:div {:class "motto vcenter"
              :style {:text-align :left}}
        [:img.logo {:src "assets/svg/logo.svg"}]
        [:p.text
         "a publishing platform for cover makers"
         [:br]
         "is coming soon."]]])))
