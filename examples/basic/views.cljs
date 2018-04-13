(ns basic.views
  (:require
    [re-frame.core :as re-frame]
    [re-structure.core :as rf]
    [basic.subs :as subs]
    [basic.events :as events]))

(defn main-panel []
  (let [name (re-frame/subscribe [::subs/name])]
    [:div
     [:div "Hello from " @name]
     [:button {:on-click #(rf/dispatch [`events/test-event])} "Dispatch valid"]
     [:button {:on-click #(rf/dispatch [`events/test-event "abc"])} "Dispatch invalid"]]))
