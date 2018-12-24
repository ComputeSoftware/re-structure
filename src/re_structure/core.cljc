(ns re-structure.core
  (:require
    [clojure.spec.alpha :as s]
    [re-frame.core :as rf]))

(defmulti event-vec (fn [e] (first e)))

(s/def ::event-vec (s/and vector?
                          (s/multi-spec event-vec (fn [tag _] tag))))

(defmulti cofx-value (fn [v] (first v)))

#?(:cljs (def debug-enabled? ^boolean goog.DEBUG))

#?(:cljs (def ^:private re-frame-init-fns (atom [])))

#?(:cljs
   (defn init!
     []
     (doseq [f @re-frame-init-fns]
       (f))
     (reset! re-frame-init-fns [])))

#?(:cljs
   (defn hide-overwriting-log-message
     []
     (re-frame.loggers/set-loggers!
       {:warn (fn [& args]
                (cond
                  (= "re-frame: overwriting" (first args)) nil
                  :else (apply js/console.log args)))})))

#?(:cljs
   (defn dispatch
     [event-vec]
     (rf/dispatch event-vec)))

#?(:cljs
   (s/fdef dispatch
           :args (s/cat :event-vec ::event-vec)
           :ret nil?))

#?(:cljs
   (defn dispatch-sync
     [event-vec]
     (rf/dispatch-sync event-vec)))

#?(:cljs
   (s/fdef dispatch-sync
           :args (s/cat :event-vec ::event-vec)
           :ret nil?))

#?(:cljs
   (defn inject-cofx
     ([id]
      (rf/inject-cofx id))
     ([id value]
      (rf/inject-cofx id value))))

#?(:cljs
   (s/fdef inject-cofx
           :args (s/multi-spec cofx-value (fn [tag _] tag))
           :ret nil?))

#?(:clj
   (defn wrap-that-call
     "Because Spec instrumentation will be enabled after fx registration, we
     need to call the function using its symbol instead of the actual function
     object."
     [handler]
     `(if debug-enabled?
        ;; Because Spec instrumentation will be enabled after fx registration,
        ;; we need to call the function using its symbol instead of the actual
        ;; function object.
        (fn [& args#]
          (apply ~handler args#))
        ~handler)))

#?(:clj
   (defn ns-qualify
     [sym]
     (symbol (str *ns*) (str sym))))

#?(:clj
   (defn reg-event-*-form
     [ctor name {::keys [spec cofx]} handler]
     `(do
        (defmethod event-vec '~name
          [_#]
          (s/cat :name #{'~name} ~@(if (= spec ::no-params) [] [:params spec])))
        (~ctor '~name
          (map (fn [[id# value#]]
                 (if value#
                   (inject-cofx id# value#)
                   (inject-cofx id#)))
               '~(map (fn [[cofx-sym# val#]]
                        [(ns-qualify cofx-sym#) val#]) cofx))
          ~handler))))

#?(:clj
   (defn reg-fx-form
     [name {::keys [spec]} handler]
     `(do
        (s/fdef ~name
                :args (s/cat :value (s/spec ~spec))
                :ret any?)
        (rf/reg-fx '~name ~(wrap-that-call handler)))))

#?(:clj
   (defn reg-cofx-form
     [name {::keys [spec]} handler]
     `(do
        (defmethod cofx-value '~name
          [_#]
          (s/cat :id #{'~name} ~@(if (= spec ::no-params) [] [:params spec])))
        (rf/reg-cofx '~name ~(wrap-that-call handler)))))

#?(:clj
   (defn reg-*-form
     [kind name opts handler]
     (case kind
       :event-db (reg-event-*-form `rf/reg-event-db name opts handler)
       :event-fx (reg-event-*-form `rf/reg-event-fx name opts handler)
       :fx (reg-fx-form name opts handler)
       :cofx (reg-cofx-form name opts handler))))

(defmacro def-rf*
  [kind name docstring attr-map params & body]
  (let [qualified-name (ns-qualify name)]
    `(let [v# (defn ~name ~@(when docstring [docstring]) ~params ~@body)]
       (swap! re-frame-init-fns conj (fn []
                                       ~(reg-*-form kind qualified-name attr-map name)))
       v#)))

(s/def ::def-*-args (s/cat :docstring (s/? string?)
                           :attr-map map?
                           :params vector?
                           :body (s/* any?)))

(defmacro def-rf
  [kind name & args]
  (let [{:keys [docstring attr-map params body]} (s/conform ::def-*-args args)]
    `(def-rf* ~kind ~name ~docstring ~attr-map ~params ~@body)))

(defmacro def-event-db
  {:arglist '([name doc-string? attr-map [params*] body])}
  [name & args]
  `(def-rf :event-db ~name ~@args))

(defmacro def-event-fx
  {:arglist '([name doc-string? attr-map [params*] body])}
  [name & args]
  `(def-rf :event-fx ~name ~@args))

(defmacro def-fx
  {:arglist '([name doc-string? attr-map [params*] body])}
  [name & args]
  `(def-rf :fx ~name ~@args))

(defmacro def-cofx
  {:arglist '([name doc-string? attr-map [params*] body])}
  [name & args]
  `(def-rf :cofx ~name ~@args))