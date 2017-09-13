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
                               :headers {"Content-Type" "multipart/form-data"}})))





(defn post-file [id]
  (let [el (sel1 id)
        file (-> el .-files (aget 0))]
    (ajax/POST "/upload-file" {:method :post
                               :body file
                               :request-format (ajax/raw-response-format)})))
