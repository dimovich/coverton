(ns coverton.fabric.views
  (:require [reagent.core   :as r]
            [react-fabricjs]
            ;;[cljsjs.fabric]
            [dommy.core     :as d  :refer-macros [sel1]]
            [taoensso.timbre :refer-macros [info]]))


(def Canvas window.fabric.Canvas)
(def Rect window.fabric.Rect)
(def Text window.fabric.Text)


(def img-url "https://www.google.com/images/branding/googlelogo/1x/googlelogo_color_272x92dp.png")

(defn fabric []
  (let [canvas (r/atom nil)]
    (r/create-class
     {:component-did-mount
      (fn [this]
        (let [c (Canvas. "canv")
              r (Rect. (clj->js {:left 50 :top 50
                                 :fill "red" :width 50
                                 :height 50}))]
          (reset! canvas c)
          (.add c r)
          (.add c (Text. "Hello World" (clj->js {:left 100 :top 100})))
          #_(.fromURL window.fabric.Image img-url (fn [img]
                                                    (info img)
                                                    (.add @canvas img)))
          ;;         (.setBackgroundImage c img-url)
          ))
      :component-did-update
      (fn [this]
        (info (r/props this))
        (window.fabric.Image.fromURL img-url (fn [img]
                                               (info img)
                                               (.add @canvas img))))
      :reagent-render
      (fn []
        [:div.editor
         [:canvas#canv]])})))
