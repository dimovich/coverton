(set-env!
 :source-paths    #{"src/cljs" "src/clj" "src/cljc"}
 :resource-paths  #{"resources"}
 :dependencies '[[org.clojure/clojure "1.9.0-beta4"]
                 [org.clojure/clojurescript "1.9.946"]

                 [adzerk/boot-cljs-repl     "0.3.3"  :scope "test"]
                 [adzerk/boot-cljs          "2.1.4"  :scope "test"]
                 [adzerk/boot-reload        "0.5.2"  :scope "test"]
                 [com.cemerick/piggieback   "0.2.2"  :scope "test"]
                 [weasel                    "0.7.0"  :scope "test"]

                 [org.clojure/tools.nrepl   "0.2.13"]
                 [cider/cider-nrepl         "0.15.1"]
                 
                 [compojure      "1.6.0"]
                 [hiccup         "2.0.0-alpha1"]
                 [http-kit       "2.2.0"]
                 [com.taoensso/timbre       "4.10.0"]
                 [javax.servlet/servlet-api "3.0-alpha-1"]

                 [ring/ring-core "1.6.3"]
                 [ring-transit   "0.1.6"]
                 [ring-middleware-format "0.7.2"]
                 [bk/ring-gzip "0.2.1"]

                 [buddy/buddy-auth "2.1.0"]
                 [buddy/buddy-hashers "1.3.0"]
                 [buddy/buddy-sign "2.2.0"]
                 [buddy/buddy-core "1.4.0"]

                 [clj-time "0.14.2"]
                 [com.draines/postal "2.0.2"]
                 [integrant "0.6.1"]
                 [com.datomic/clj-client "0.8.606"]
                 [org.clojure/core.async "0.3.465"]
                 [org.clojure/data.fressian "0.2.1"]
                 
                 [prismatic/dommy "1.1.0"]
                 [reagent  "0.8.0-alpha2"]
                 [re-frame "0.10.2"]
                 [cljs-ajax "0.7.3"]
                 [day8.re-frame/http-fx "0.1.4"]
                 ;;[day8.re-frame/trace "0.1.11"]
                 [com.taoensso/tengen "1.0.0-RC1"]
                 [jkkramer/verily "0.6.1"]

                 [cljsjs/fabric "1.7.19-1"]])


(require
 '[adzerk.boot-cljs      :refer [cljs]]
 '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
 '[adzerk.boot-reload    :refer [reload]]
 '[coverton.system       :as    system])


(task-options! jar   {:main 'coverton.core :file "coverton.jar"}
               sift  {:include
                      #{#"coverton[.]jar" #"coverton[.]js$" #"assets"
                        #"config.edn$" #"uploads/.*jpg$" #"db/.*edn$"}}
               aot   {:namespace #{'coverton.core}}
               cljs  {:compiler-options
                      {:output-to  "public/coverton.js"
                       :output-dir "public/out"
                       :asset-path "out"
                       :warnings {:redef false}
                       :parallel-build true
                       ;;:pseudo-names true
                       ;;:closure-defines      {"re_frame.trace.trace_enabled_QMARK_" true}
                       ;;:preloads             ['day8.re-frame.trace.preload]
                       }})
                       
                       
                       


(deftask production
  []
  (task-options! cljs   {:optimizations :advanced}
                 target {:dir #{"release"}})
  identity)


(deftask development
  []
  (task-options! cljs      {:optimizations :none
                            :source-map    true}
                 cljs-repl {:nrepl-opts {:port 3311}}
                 target    {:dir #{"target"}})
  identity)


(deftask build-jar
  []
  (comp
   (aot)
   (uber)
   (jar)
   (sift)))


(deftask run []
  (comp
   (watch)
   (reload)
   (cljs-repl)
   (cljs)
   (target)
   (call :eval (system/init {:path "resources/config.edn"})
         :once true
         :post true)))


(deftask dev
  []
  (task-options! reload {:on-jsload 'coverton.core/reload})
  (comp
   (development)
   (cider)
   (run)))


(deftask prod
  []
  (comp
   (production)
   (cljs)
   (build-jar)
   (target)))
