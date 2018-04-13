(defproject re-structure "0.1.0-SNAPSHOT"
  :dependencies [[thheller/shadow-cljs "2.2.27"]
                 [binaryage/devtools "0.9.9"]

                 [org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.10.238"]
                 [org.clojure/test.check "0.9.0"]

                 [reagent "0.7.0"]
                 [re-frame "0.10.5"]
                 [day8.re-frame/re-frame-10x "0.2.1"]

                 [expound "0.5.0"]]
  :plugins [[lein-ancient "0.6.15"]
            [lein-cooper "1.2.2"]]
  :min-lein-version "2.5.3"
  :source-paths ["src" "examples"]
  :profiles {:dev {:cooper {"cljs" ["shadow-cljs" "watch" "app"]}}}
  :aliases {"dev" ["with-profile" "dev" "cooper"]})
