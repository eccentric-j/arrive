(ns app.api.stations
  (:require
   [app.config :refer [config]]
   [mta.stations :as stations]))

(defn get-stations
  [req]
  {:status 200
   :body (stations/fetch config)})

(def api
  {:get {:parameters {}
         :responses {200 {:body seq?}}
         :handler get-stations}})
