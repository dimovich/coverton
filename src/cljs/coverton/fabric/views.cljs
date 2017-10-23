(ns coverton.fabric.views
  (:require [reagent.core       :as r]
            [re-frame.core      :as rf :refer [reg-event-fx]]
            [coverton.ed.subs   :as ed-sub]
            [coverton.ed.events :as ed-evt :refer [cover-interceptors]]
            [coverton.index.subs :as index-sub]
            [re-frame.core      :refer [subscribe dispatch]]
            [taoensso.timbre    :refer-macros [info]]
            [coverton.fonts     :refer [default-font]]
            [coverton.components :as cc]
            [coverton.util      :as util]
            [cljsjs.fabric]))



(def Canvas  window.fabric.Canvas)
(def Rect    window.fabric.Rect)
(def Text    window.fabric.Text)
(def IText   window.fabric.IText)
(def fromURL  window.fabric.Image.fromURL)
(def loadSVGFromString window.fabric.loadSVGFromString)
(def groupSVGElements window.fabric.util.groupSVGElements)
(def enlivenObjects window.fabric.util.enlivenObjects)


(def canvas (atom nil))


(defn click->relative [e]
  (let [bounds (.. e -target getBoundingClientRect)
        x (- (.. e -clientX) (.. bounds -left))
        y (- (.. e -clientY) (.. bounds -top))]
    [x y]))



(defn add-mark [canvas [x y]]
  (let [mark (->> {:left x :top y
                   :fill (:color default-font) :cursorColor (:color default-font)
                   :fontFamily (:font-family default-font) :fontSize 40}
                  clj->js
                  (IText. "hello"))]
    (.add canvas mark)))



(defn on-click-add-mark [canvas evt]
  (->> (.. evt -e)
       click->relative
       (add-mark canvas)))



(reg-event-fx
 ::fabric->cover
 cover-interceptors
 (fn [{db :db} [canvas]]
   {:db (merge db {:cover/fabric
                   {:json (js->clj (.toJSON canvas))
                    :svg  (.toSVG canvas)}})}))


(defn fabric->cover [canvas]
  (dispatch [::fabric->cover canvas]))




(defn set-background [canvas url]
  (info "setting background: " url)
  (fromURL url
           (fn [img]
             (.scaleToWidth img (.. canvas -width))
             (.setBackgroundImage canvas img
                                  #(do (.renderAll canvas)
                                       (fabric->cover canvas))))))



(defn upload-artifacts []
  (when-let [file (util/form-data :#image-input)]
    (info "uploading image...")
    (dispatch [::upload-file file
               {:on-success [::ed-evt/merge-cover]}])))


#_(defn save-cover []
    (upload-artifacts))


(defn attach-events [canvas]
  (let [save #(do (info "saving...")
                  (fabric->cover canvas))
        selecting? (atom false)]
    (doto canvas
      (.on (clj->js
            {"object:modified" save
             "object:added"    save
             "object:removed"  save
             "selection:created" #(reset! selecting? true)
             "mouse:down" #(reset! selecting? false)
             "mouse:up"
             (fn [evt]
               ;; when clicking some empty space
               (when-not (or @selecting? (.. evt -target))
                 (on-click-add-mark canvas evt)))})))))




(defn cover->fabric [canvas cover]
  ;;(.clear canvas)
  (let [url (:cover/image-url cover)
        fabric (:cover/fabric cover)]
    (if-let [json (clj->js (:json fabric))]
      (do
        (info "loading from json..." json)
        (.loadFromJSON canvas json
                       (fn []
                         (.renderAll canvas)
                         (attach-events canvas))))
      ;; new
      (do
        (set-background canvas url)
        (attach-events canvas)))))




(defn set-canvas-size [canvas parent]
  (let [h (.. parent -clientHeight)]
    (info "setting size" h h)
    (doto canvas
      (.setWidth h)
      (.setHeight h))))



(defn init-fabric [canvas])


(defn fabric [{url :cover/image-url}]
  (let [parent-dom (atom nil)
        cover (subscribe [::ed-sub/cover])]
    (r/create-class
     {:component-did-mount
      (fn [_]
        (reset! canvas   (Canvas. "canv"))
        
        (init-fabric     @canvas)
        (set-canvas-size @canvas @parent-dom)
        (cover->fabric   @canvas @cover)
        (fabric->cover   @canvas))
    
      :component-did-update
      (fn [this]
        (info "updating fabric...")
        (set-background @canvas
                        (:cover/image-url (r/props this))))

      :component-will-unmount
      (fn [this]
        (info "unmounting fabric...")
        (some-> @canvas .clear))

      :reagent-render
      (fn []
        [:div.fabric-wrap
         [:div.editor-img {:ref #(reset! parent-dom %)}
          [:canvas#canv]]])})))




(defn editor [{initial-cover :cover}]
  (r/with-let [_     (ed-evt/initialize initial-cover)
               cover (subscribe [::ed-sub/cover])
               auth? (subscribe [::index-sub/authenticated?])
               ed-t  (subscribe [::ed-sub/t])
               url   (subscribe [::ed-sub/image-url])]

    ^{:key @ed-t}
    
    [:div.editor
     [:div.header {:style {:text-align :center
                           :margin "0.5em auto"}}
      (cc/menu
       [cc/image-picker]
       
       (when @auth?
         [:a {:on-click #(ed-evt/save-cover)}
          "save"]))]

     [fabric {:cover/image-url @url}]

     [:br]
     [:div (str @cover)]]))



;; TODO:
;;
;; scale
;; picker-block
;; controls
;; background
