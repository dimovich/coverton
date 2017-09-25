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

