;; scp -rp release/* root@coverton.co:/var/www/coverton-co/



;; dumb component
(r/with-let [[x y] some-dynamic-var]
  (;;use x y
   ))

;; smart components
(r/with-let [s (subscribe [:key])]
  @s)



;;--- Fetch/Set (take server from namen)

(reg-event-db
 :process-notes-list
 (fn [db [_ response]]
   (-> db
       (assoc :notes-list-answered? true)
       (assoc :notes-list response))))

(reg-event-db
 :load-notes-list
 (fn [db _]
   (GET "/api/list-notes"
        {:format  :json
         :handler #(dispatch [:process-notes-list %])})
   db))


;;---

