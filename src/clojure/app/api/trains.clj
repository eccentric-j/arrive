(ns app.api.trains
  (:require
   [app.config :refer [config]]
   [mta.core :as mta]))

(defn get-trains
  [req]
  {:status 200
   :body (mta/train-arrivals config)})

(def api
  {:get {:parameters {}
         :responses {200 {:body seq?}}
         :handler get-trains}})
