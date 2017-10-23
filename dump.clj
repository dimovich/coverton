(defn up-file [id]
  (let [el (sel1 id)
        file (-> el .-files (aget 0))]
    (dispatch [::evt/upload-file file])))

(defn up-form [id]
  (let [el (sel1 id)
        file (js/FormData. el)]
    (dispatch [::evt/upload-file file])))




(defn post-form [id]
  (let [el (sel1 id)
        file (js/FormData. el)]
    (ajax/POST "/upload-file" {:body file
                               ;;:headers {"Content-Type" "multipart/form-data"}
                               })))





(defn post-file [id]
  (let [el (sel1 id)
        file (-> el .-files (aget 0))]
    (ajax/POST "/upload-file" {:method :post
                               :body file
                               :request-format (ajax/raw-response-format)})))




(defn upload-file [element-id]
  (let [el (sel1 element-id)
        file (aget (.-files el) 0)
        form-data (doto
                      (js/FormData.)
                      (.append "file" file))]
    (ajax/POST "/upload-file"
               {:body form-data})))









(defn merge-props [res new]
  (if (map? res)
    (merge-with merge res new)
    new))



;;datomic + jetty lib issues
[com.datomic/datomic-pro "0.9.5561" :exclusions [org.slf4j/slf4j-nop com.google.guava/guava]]



;; secret client power
(when (and @(subscribe [::sub/authenticated?])
           (= "dimovich" @(subscribe [::sub/user])))
  [cc/Button {:style {:position :fixed
                      :bottom 0 :right 0}
              :on-click #(dispatch [::evt-ajax/request-auth {:uri "export-db"}])}
   "Export DB"])











(let [f (io/file "db/users.edn")]
  (when (.exists f)
   (with-open [rdr (io/reader f)]
     (->> rdr
          slurp
          println))))




(let [f (io/file "db/users.edn")]
  (when (.exists f)
    (->> f
         slurp
         println)))



(def f (io/file "db/users.edn"))

f
(.exists f)




(defn import-db-file [fname & [{f :fn :or {f identity}}]]
  (let [file (io/file fname)]
    (when (.exists file)
      (with-open [rdr (io/reader file)]
        (some->> (slurp rdr)
                 read-string
                 (map f)
                 add-data)))))



(defmacro when-read [[name fname] & body]
  `(let [file# (io/file ~fname)]
     (when (.exists file#)
       (with-open [rdr# (io/reader file#)]
         (let [~name (slurp rdr#)]
           ~@body)))))






(require 'coverton.db.core)
(in-ns 'coverton.db.core)


(let [id (:cover/id (second (get-all-covers)))]
  ;;(retract-entity [:cover/id id])
  ;;(retract-attr [:cover/id id] :cover/tags "dimovich")
  (transact [{:cover/id id
              :cover/tags ["radyon"]}]))



(defn retract-entity [id]
  (add-data [[:db.fn/retractEntity id]]))

(get-all-covers)






#_(rename-keys cover->db-map)
#_(update-in [:cover/marks]
             #(map (fn [m] (rename-keys m mark->db-map)) %))



#_(serve {:resource-root "target/public"
          :handler 'coverton.core/app
          :reload  true
          :httpkit true
          :init 'coverton.core/init})



;; default value is fabric compatible


(swap! items assoc (random-uuid) {:fabric (.fromObject window.fabric.Text (clj->js obj))})


(.fromURL window.fabric.Image img-url (fn [img]
                                        (info img)
                                        (.add @canvas img)))


(.setBackgroundImage c img-url)


(window.fabric.Image.fromURL img-url (fn [img]
                                       (.add @canvas img)))








(deftask cider "CIDER profile"
  []
  (require 'boot.repl)
  (swap! boot.repl/*default-dependencies*
         concat '[[cider/cider-nrepl "0.15.1"]])
  (swap! boot.repl/*default-middleware*
         concat '[cider.nrepl/cider-middleware])
  identity)







{:cover {:fabric [..]}}

:component-will-unmount



(.toSVG @canvas (clj->js {:suppressPreamble true}))



(def svg "<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" version=\"1.1\" width=\"475\" height=\"475\" xml:space=\"preserve\"><desc>Created with Fabric.js 1.5.0</desc><defs></defs><g transform=\"translate(237.01 237.01) scale(0.99 0.99)\">\n<image xlink:href=\"http://localhost:5000/assets/img/coverton.jpg\" x=\"-240\" y=\"-240\" style=\"stroke: none; stroke-width: 1; stroke-dasharray: ; stroke-linecap: butt; stroke-linejoin: miter; stroke-miterlimit: 10; fill: rgb(0,0,0); fill-rule: nonzero; opacity: 1;\" width=\"480\" height=\"480\" preserveAspectRatio=\"none\"></image>\n</g>\n\t<g transform=\"translate(111.5 103.47)\">\n\t\t<text font-family=\"GothaPro\" font-size=\"61.75\" font-weight=\"normal\" style=\"stroke: none; stroke-width: 1; stroke-dasharray: ; stroke-linecap: butt; stroke-linejoin: miter; stroke-miterlimit: 10; fill: #FF9933; fill-rule: nonzero; opacity: 1;\" ><tspan x=\"-73.4824\" y=\"13.8691\" fill=\"#FF9933\">hello</tspan></text>\n\t</g>\n\t<g transform=\"translate(420.5 145.47)\">\n\t\t<text font-family=\"GothaPro\" font-size=\"61.75\" font-weight=\"normal\" style=\"stroke: none; stroke-width: 1; stroke-dasharray: ; stroke-linecap: butt; stroke-linejoin: miter; stroke-miterlimit: 10; fill: #FF9933; fill-rule: nonzero; opacity: 1;\" ><tspan x=\"-73.4824\" y=\"13.8691\" fill=\"#FF9933\">hello</tspan></text>\n\t</g>\n\t<g transform=\"translate(135.5 415.47)\">\n\t\t<text font-family=\"GothaPro\" font-size=\"61.75\" font-weight=\"normal\" style=\"stroke: none; stroke-width: 1; stroke-dasharray: ; stroke-linecap: butt; stroke-linejoin: miter; stroke-miterlimit: 10; fill: #FF9933; fill-rule: nonzero; opacity: 1;\" ><tspan x=\"-73.4824\" y=\"13.8691\" fill=\"#FF9933\">hello</tspan></text>\n\t</g>\n</svg>")


(loadSVGFromString
 svg
 (fn [objs opts]
   (map #(.add @canvas %) objs)
   #_(let [group (groupSVGElements objs opts)]
       (info objs)
       (.. @canvas
           (add objs)
           renderAll))))







(loadSVGFromString
 svg
 (fn [objs opts]))




#js {:objects #js [#js {:type "i-text", :originX "left", :originY "top", :left 45.02, :top 166, :width 93.12, :height 51.29, :fill "#FF9933", :stroke nil, :strokeWidth 1, :strokeDashArray nil, :strokeLineCap "butt", :strokeLineJoin "miter", :strokeMiterLimit 10, :scaleX 1, :scaleY 1, :angle 0, :flipX false, :flipY false, :opacity 1, :shadow nil, :visible true, :clipTo nil, :backgroundColor "", :fillRule "nonzero", :globalCompositeOperation "source-over", :text "hello", :fontSize 39.13, :fontWeight "normal", :fontFamily "GothaPro", :fontStyle "", :lineHeight 1.16, :textDecoration "", :textAlign "left", :textBackgroundColor "", :styles #js {}} #js {:type "i-text", :originX "left", :originY "top", :left 63.02, :top 87, :width 93.12, :height 51.29, :fill "#FF9933", :stroke nil, :strokeWidth 1, :strokeDashArray nil, :strokeLineCap "butt", :strokeLineJoin "miter", :strokeMiterLimit 10, :scaleX 1, :scaleY 1, :angle 0, :flipX false, :flipY false, :opacity 1, :shadow nil, :visible true, :clipTo nil, :backgroundColor "", :fillRule "nonzero", :globalCompositeOperation "source-over", :text "hello", :fontSize 39.13, :fontWeight "normal", :fontFamily "GothaPro", :fontStyle "", :lineHeight 1.16, :textDecoration "", :textAlign "left", :textBackgroundColor "", :styles #js {}} #js {:type "i-text", :originX "left", :originY "top", :left 258.02, :top 54, :width 93.12, :height 51.29, :fill "#FF9933", :stroke nil, :strokeWidth 1, :strokeDashArray nil, :strokeLineCap "butt", :strokeLineJoin "miter", :strokeMiterLimit 10, :scaleX 1, :scaleY 1, :angle 0, :flipX false, :flipY false, :opacity 1, :shadow nil, :visible true, :clipTo nil, :backgroundColor "", :fillRule "nonzero", :globalCompositeOperation "source-over", :text "hello", :fontSize 39.13, :fontWeight "normal", :fontFamily "GothaPro", :fontStyle "", :lineHeight 1.16, :textDecoration "", :textAlign "left", :textBackgroundColor "", :styles #js {}}], :background "", :backgroundImage #js {:type "image", :originX "left", :originY "top", :left 0, :top 0, :width 480, :height 480, :fill "rgb(0,0,0)", :stroke nil, :strokeWidth 1, :strokeDashArray nil, :strokeLineCap "butt", :strokeLineJoin "miter", :strokeMiterLimit 10, :scaleX 0.63, :scaleY 0.63, :angle 0, :flipX false, :flipY false, :opacity 1, :shadow nil, :visible true, :clipTo nil, :backgroundColor "", :fillRule "nonzero", :globalCompositeOperation "source-over", :src "http://localhost:5000/assets/img/coverton.jpg", :filters #js [], :crossOrigin "", :alignX "none", :alignY "none", :meetOrSlice "meet"}}




(loadSVGFromString
 svg
 (fn [objs opts]
   (enlivenObjects objs
                   (fn [objs]
                     (doseq [o objs] (info o) #_(.add @canvas o))))))




(loadSVGFromString
 svg
 (fn [objs opts]
   (parseElements objs
                  (fn [objs]
                    (info objs)))))




(loadSVGFromString
 svg
 (fn [objs opts]
   (doseq [o objs]
     (.add @canvas o))))




(defn click->relative [e]
  (let [bounds (.. e -target getBoundingClientRect)
        x (- (.. e -clientX) (.. bounds -left))
        y (- (.. e -clientY) (.. bounds -top))]
    [x y]))


#_[(/ x (.. bounds -width))
   (/ y (.. bounds -height))]





(defn cover-image [{url :url size :size}]
  (r/with-let [this        (r/current-component)
               update-size (fn [e]
                             (->> (.. e -target getBoundingClientRect)
                                  ((juxt #(.. % -width) #(.. % -height)))
                                  (reset! size)))]
    
    [:img.cover-image {:on-load  update-size
                       :src      url}]))



#_{:dangerouslySetInnerHTML
        {:__html (str  "</img>")}}





;;
;; calculate text width in px for font type and size
;; and change element width.
;; Uses a static span element #span-measure
;;
(defn set-width [el]
  (let [font (d/style el :font-family)
        size (d/style el :font-size)
        span (sel1 :#span-measure)]

    ;; copy styles to span
    (d/set-style! span :font-size size)
    (d/set-style! span :font-family font)
    (d/set-html!  span "")
    (d/append!    span (d/create-text-node (d/value el))) ;;fixme: memleak?

    ;; get normal width (has issues with whitespace),
    ;; so possibly extend to scroll width
    (d/set-px! el :width (+ 2 (.. span -scrollWidth)))
    (d/set-px! el :width (.. el -scrollWidth))))

