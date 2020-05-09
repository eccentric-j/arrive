(ns app.server
  (:require
   [org.httpkit.server :as http]
   [reitit.ring :as ring]
   [reitit.coercion.spec :as rc]
   [reitit.ring.coercion :as rrc]
   [ring.middleware.reload :refer [wrap-reload]]
   [ring.middleware.content-type :refer [wrap-content-type]]
   [ring.middleware.json :refer [wrap-json-response]]
   [app.api.trains :as trains]
   [app.api.stations :as stations]))

(def ^:private not-found-handler
  (fn [req]
    {:status  404
     :headers {"Content-Type" "text/plain"}
     :body    "Not Found"}))

(def app
  (ring/ring-handler
   (ring/router
    [["/" (fn [_] {:status 200
                   :headers {"Content-Type" "text/plain"}
                   :body "Hello World"})]
     ["/api" [["/trains" trains/api]
              ["/stations" stations/api]]]]
    {:data {:middleware [wrap-reload
                         wrap-content-type
                         wrap-json-response]}})
   not-found-handler))

(defonce stop-fn (atom nil))

(defn start
  []
  (reset! stop-fn (http/run-server app {:port 8585})))

(defn stop
  []
  (when @stop-fn
    (@stop-fn)
    (reset! stop-fn nil)))
