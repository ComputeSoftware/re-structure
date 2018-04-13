(ns basic.events
  (:require
    [clojure.spec.alpha :as s]
    [re-structure.core :include-macros true :as rf]
    [basic.db :as db]))

(rf/def-event-db initialize-db
  ""
  {::rf/spec ::rf/no-params}
  [_ _]
  db/default-db)

(rf/def-fx my-fx
  ""
  {::rf/spec (s/cat :x int? :y int?)}
  [[x y z]]
  (println "x y" x y z))

(rf/def-cofx
  my-cofx
  ""
  {::rf/spec ::rf/no-params}
  [cofx]
  (assoc cofx :rand-int (rand-int 10)))

(rf/def-event-fx
  test-event
  ""
  {::rf/spec ::rf/no-params
   ::rf/cofx [[`my-cofx]]}
  [{:keys [rand-int]} _]
  (do
    (println "called test event!" rand-int)
    {`my-fx [1 2]}))

