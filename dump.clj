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
