;; scp -rp sourcedirectory user@dest:/path





;;--- Fetch/Set

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


---
