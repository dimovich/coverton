(ns coverton.validations
  (:require [jkkramer.verily :as v]))



(def validations
  {:request-invite [[:required [:email :story]]]

   :login [[:required [:email :password]]]

   :email [;;[:email :email]
           [:not-blank :email]]

   :story [[:not-blank :story]]

   :password [[:not-blank :password]]})



(defn validate [m & ks]
  (apply
   (partial merge-with concat)
   (for [k ks]
     (->> (get validations k)
          (v/validate m)
          (reduce
           (fn [res m]
             (->> (repeat (list (:msg m)))
                  (zipmap (:keys m))
                  (merge-with concat res)))
           {})))))



(defn validator [state errors]
  (fn [& ks]
    (let [msgs (apply validate @state ks)
          good (apply disj (set ks) (keys msgs))]
      (swap! errors #(-> (apply dissoc % good)
                         (merge msgs))))))
