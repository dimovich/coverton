(set-env!
 :source-paths #{"src/clj" "src/cljs"}
 :resource-paths #{"html"}
 :dependencies '[[org.clojure/clojure "1.9.0-alpha16"]
                 [org.clojure/clojurescript "1.9.542"]
                 [adzerk/boot-cljs "2.0.0"]
                 [pandeiro/boot-http "0.8.3"]
                 [adzerk/boot-reload "0.5.1"]
                 [adzerk/boot-cljs-repl   "0.3.3"]
                 [com.cemerick/piggieback "0.2.1"  :scope "test"]
                 [weasel                  "0.7.0"  :scope "test"]
                 [org.clojure/tools.nrepl "0.2.12" :scope "test"]
                 [compojure "1.6.0"]
                 [javax.servlet/servlet-api "3.0-alpha-1"]
                 [hiccup "1.0.5"]
                 [prismatic/dommy "1.1.0"]
                 [reagent "0.6.1"]
                 [hipo "0.5.2"]])

(require '[pandeiro.boot-http :refer [serve]]
         '[adzerk.boot-cljs :refer [cljs]]
         '[adzerk.boot-reload :refer [reload]]
         '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
         'boot.repl)

(swap! boot.repl/*default-dependencies*
       concat '[[cider/cider-nrepl "0.15.0-SNAPSHOT"]])

(swap! boot.repl/*default-middleware*
       conj 'cider.nrepl/cider-middleware)


(deftask dev []
  (comp
   (serve :resource-root "target"
          :handler 'coverton.core/app)
   (watch)
   (reload)
   (cljs-repl)
   (cljs :compiler-options {:out-file "main.js"})
   (target :dir #{"target"})))


(deftask release []
  (comp
   (cljs :compiler-options {:optimizations :advanced})
   (target :dir #{"target"})))
