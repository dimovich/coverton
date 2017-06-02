(set-env!
 :source-paths    #{"src/cljs" "src/clj"}
 :resource-paths  #{"html" "public"}
 :dependencies '[[org.clojure/clojure "1.9.0-alpha17" :scope "provided"]
                 [org.clojure/clojurescript "1.9.562"]

                 [adzerk/boot-cljs          "2.0.0"  :scope "test"]
                 [adzerk/boot-cljs-repl     "0.3.3"      :scope "test"]
                 [adzerk/boot-reload        "0.5.1"      :scope "test"]
                 [pandeiro/boot-http        "0.8.3"      :scope "test"]
                 [com.cemerick/piggieback   "0.2.1"      :scope "test"]
                 [org.clojure/tools.nrepl   "0.2.12"     :scope "test"]
                 [weasel                    "0.7.0"      :scope "test"]
                 [tolitius/boot-check       "0.1.4"]

                 [compojure "1.6.0"]
                 [javax.servlet/servlet-api "3.0-alpha-1"]
                 [hiccup "1.0.5"]

                 [devcards "0.2.3" :exclusions [cljsjs/react cljsjs/react-dom]]

                 [prismatic/dommy "1.1.0"]
                 ;;[soda-ash "0.2.0" :exclusions [cljsjs/react cljsjs/react-dom]]
                 [reagent "0.6.1" :exclusions [cljsjs/react cljsjs/react-dom]]
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


(deftask build []
  (comp
   (cljs :compiler-options
         {:out-file "main.js"
          :devcards true
          :foreign-libs
          [{:file "public/js/bundle.js"
            :provides ["cljsjs.react" "cljsjs.react.dom"]}]})
   (target :dir #{"target"})))

(deftask run []
  (comp
   (serve :resource-root "target"
          :handler 'coverton.core/app
          :reload true
          :httpkit true)
   (watch)
   (reload)
   (cljs-repl)
   (build)))

(deftask production
  []
  (task-options! cljs {:optimizations :advanced})
  identity)

(deftask development
  []
  (task-options! cljs {:optimizations :none
                       :source-map true}
                 cljs-repl {:nrepl-opts {:port 3311}})
  identity)


(deftask reload-main
  []
  (task-options! reload {:on-jsload 'coverton.core/main})
  identity)


(deftask dev
  []
  (comp (development)
        (reload-main)
        (run)))


(deftask devcards
  []
  (set-env! :source-paths #(conj % "src/devcards"))
  (comp (development)
        (run)))

(deftask prod
  []
  (comp (production)
        (build)))


(deftask check-sources
  []
  (comp
    (check/with-yagni)
    (check/with-eastwood)
    (check/with-kibit)
    (check/with-bikeshed)))
