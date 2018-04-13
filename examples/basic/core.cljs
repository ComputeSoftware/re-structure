(ns basic.core
  (:require
    [cljs.spec.alpha :as s]
    [cljs.spec.test.alpha :as st]
    [re-structure.core :as rf]
    [expound.alpha :as expound]
    [reagent.core :as reagent]
    [re-frame.core :as re-frame]
    [basic.events :as events]
    [basic.views :as views]
    [basic.config :as config]))

(defn dev-setup
  []
  (when config/debug?
    (enable-console-print!)
    (st/instrument)
    (set! s/*explain-out* expound/printer)
    (println "dev mode")))

(defn mount-root
  []
  (rf/init!)
  (st/instrument)
  (rf/hide-overwriting-log-message)
  (re-frame/clear-subscription-cache!)
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn on-reload
  []
  (mount-root))

(defn ^:export init
  []
  (dev-setup)
  (rf/init!)
  (re-frame/dispatch-sync [`events/initialize-db])
  (mount-root))
