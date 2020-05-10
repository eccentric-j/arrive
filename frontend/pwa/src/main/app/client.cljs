(ns app.client
  (:require
   [reagent.core :as r]
   [reagent.dom :as rdom]
   [clojure.string :as str]
   [kitchen-async.promise :as p]))

(def trains (r/atom []))

(defn fetch-arrivals
  []
  (p/let [arrivals (p/-> (js/fetch "/api/trains")
                         (.json)
                         (js->clj :keywordize-keys true))]
    (reset! trains arrivals)))

(defn arrivals
  []
  (if-let [trains @trains]
   [:div
    (for [train trains]
      [:div
       {:key (:train/id train)}
       (:train/name train)])]))

(defn simple-example
  []
  [:div
   [:h1 "Arrive"]
   [arrivals]
   [:button
    {:on-click fetch-arrivals}
    "Fetch Arrival Times"]])

(set! (.-onload js/window)
      (fn []
        (when-let [service-worker (.-serviceWorker js/navigator)]
          (.register service-worker "./worker.js" ))))

(defn ^:export init
  ""
  []
  (rdom/render [simple-example] (js/document.getElementById "app"))
  (js/console.log "Loaded"))

(defn ^:export refresh
  []
  (rdom/render [simple-example] (js/document.getElementById "app"))
  (js/console.log "Refresh"))
