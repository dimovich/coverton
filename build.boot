(set-env!
 :source-paths    #{"src/cljs" "src/clj"}
 :resource-paths  #{"html"}
 :dependencies '[[adzerk/boot-cljs          "2.0.0"  :scope "test"]
                 [adzerk/boot-cljs-repl     "0.3.3"      :scope "test"]
                 [adzerk/boot-reload        "0.5.1"      :scope "test"]
                 [pandeiro/boot-http        "0.8.3"      :scope "test"]
                 [com.cemerick/piggieback   "0.2.1"      :scope "test"]
                 [org.clojure/tools.nrepl   "0.2.12"     :scope "test"]
                 [weasel                    "0.7.0"      :scope "test"]

                 [org.clojure/clojurescript "1.9.293"]
                 
                 [compojure "1.6.0"]
                 [javax.servlet/servlet-api "3.0-alpha-1"]
                 [hiccup "1.0.5"]
                 [prismatic/dommy "1.1.0"]
                 [reagent "0.6.1"]
                 [hipo "0.5.2"]])

(require
 '[adzerk.boot-cljs      :refer [cljs]]
 '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
 '[adzerk.boot-reload    :refer [reload]]
 '[pandeiro.boot-http    :refer [serve]])

(swap! boot.repl/*default-dependencies*
       concat '[[cider/cider-nrepl "0.15.0-SNAPSHOT"]])

(swap! boot.repl/*default-middleware*
       conj 'cider.nrepl/cider-middleware)



(deftask build []
  (comp (speak)
        (cljs)))

(deftask run []
  (comp (serve :resource-root "target"
               :handler 'coverton.core/app)
        (watch)
        (cljs-repl)
        (reload)
        (build)
        (target :dir #{"target"})))

(deftask production []
  (task-options! cljs {:optimizations :advanced})
  identity)

(deftask development []
  (task-options! cljs {:optimizations :none}
                 reload {:on-jsload 'coverton.core/init})
  identity)

(deftask dev
  
  "Simple alias to run application in development mode"
  []
  (comp (development)
        (run)))
