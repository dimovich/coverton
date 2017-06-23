(set-env!
 :source-paths    #{"src/cljs" "src/clj"}
 :resource-paths  #{"resources" }
 :dependencies '[[org.clojure/clojure "1.9.0-alpha17" :scope "provided"]
                 [org.clojure/clojurescript "1.9.562" :scope "provided"]

                 [adzerk/boot-cljs          "2.0.0"      :scope "test"]
                 [adzerk/boot-cljs-repl     "0.3.2"      :scope "test"]
                 [adzerk/boot-reload        "0.5.1"      :scope "test"]
                 [pandeiro/boot-http        "0.8.3"      :scope "test"]
                 [com.cemerick/piggieback   "0.2.1"      :scope "test"]
                 [org.clojure/tools.nrepl   "0.2.13"     :scope "test"]
                 [weasel                    "0.7.0"      :scope "test"]
                 [tolitius/boot-check       "0.1.4"      :scope "test"]

                 [compojure "1.6.0"]
                 [hiccup    "2.0.0-alpha1"]
                 [http-kit  "2.2.0"]
                 [ring/ring-core "1.6.1"]
                 [javax.servlet/servlet-api "3.0-alpha-1"]

                 [namen "0.1.0"]
                 [cheshire "5.6.3"]
                 
                 ;;[devcards "0.2.3" :exclusions [cljsjs/react cljsjs/react-dom]]

                 [prismatic/dommy "1.1.0"]
                 [reagent  "0.6.2" :exclusions [cljsjs/react cljsjs/react-dom]]
                 [re-frame "0.9.4"]])


(require
 '[adzerk.boot-cljs      :refer [cljs]]
 '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
 '[adzerk.boot-reload    :refer [reload]]
 '[pandeiro.boot-http    :refer [serve]]
 '[tolitius.boot-check   :as check])


(swap! boot.repl/*default-dependencies*
       concat '[[cider/cider-nrepl "0.15.0-SNAPSHOT"]])

(swap! boot.repl/*default-middleware*
       conj 'cider.nrepl/cider-middleware)


(deftask build-jar
  []
  (comp (aot  :namespace #{'coverton.core})
        (uber)
        (jar  :main 'coverton.core :file "app.jar")
        (sift :include #{#"app.jar" #"main.js"})))


(deftask run []
  (comp
   (serve :resource-root "target/public"
          :handler 'coverton.core/app
          :reload true
          :httpkit true)
   (watch)
   (reload)
   (cljs-repl)
   (cljs)
   (target)))


(deftask production
  []
  (task-options! cljs {:optimizations :advanced}
                 target {:dir #{"release"}})
  identity)


(deftask development
  []
  (task-options! cljs      {:optimizations :none
                            :source-map    true}
                 cljs-repl {:nrepl-opts {:port 3311}}
                 target {:dir #{"target"}})
  identity)



(deftask dev
  []
  (task-options! reload {:on-jsload 'coverton.core/reload})
  (comp (development)
        (run)))


(deftask devcards
  []
  (set-env! :source-paths #(conj % "src/devcards"))
  (task-options! reload {:on-jsload 'coverton.devcards/reload})
  (comp (development)
        (run)))


(deftask prod
  []
  (comp (production)
        (cljs)
        (build-jar)
        (target)))


(deftask check-sources
  []
  (comp
    (check/with-yagni)
    (check/with-eastwood)
    (check/with-kibit)
    (check/with-bikeshed)))
