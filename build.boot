(set-env!
 :source-paths    #{"src/cljs" "src/clj"}
 :resource-paths  #{"html"}
 :dependencies '[[org.clojure/clojure "1.9.0-alpha16" :scope "provided"]
                 [org.clojure/clojurescript "1.9.542"]

                 [adzerk/boot-cljs          "2.0.0"  :scope "test"]
                 [adzerk/boot-cljs-repl     "0.3.3"      :scope "test"]
                 [adzerk/boot-reload        "0.5.1"      :scope "test"]
                 [pandeiro/boot-http        "0.8.3"      :scope "test"]
                 [com.cemerick/piggieback   "0.2.1"      :scope "test"]
                 [org.clojure/tools.nrepl   "0.2.12"     :scope "test"]
                 [weasel                    "0.7.0"      :scope "test"]

                 [compojure "1.6.0"]
                 [javax.servlet/servlet-api "3.0-alpha-1"]
                 [hiccup "1.0.5"]
                 
                 [prismatic/dommy "1.1.0"]
                 [cljsjs/react-drag "0.2.7-0"]
                 [reagent "0.6.1" :exclusions [cljsjs/react cljsjs/react-dom]]
                 [cljsjs/react "15.5.4-0"]
                 [cljsjs/react-dom "15.5.4-0"]
                 [metosin/komponentit "0.3.0"]])


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
  (comp (cljs :compiler-options {:out-file "main.js"})
        (target :dir #{"target"})))

(deftask run []
  (comp (serve :resource-root "target"
               :handler 'coverton.core/app
               :reload true
               :httpkit true)
        (watch)
        (reload)
        (cljs-repl)
        (build)))

(deftask production []
  (task-options! cljs {:optimizations :advanced})
  identity)

(deftask development []
  (task-options! cljs {:optimizations :none}
                 reload {:on-jsload 'coverton.core/init})
  identity)

(deftask dev
  []
  (comp (development)
        (run)))


(deftask prod
  []
  (comp (production)
        (build)))
