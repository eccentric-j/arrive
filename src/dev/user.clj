(ns user
  (:require
   [app.server :as server]
   [clojure.tools.namespace.repl
    :as tools-ns
    :refer [set-refresh-dirs refresh]]))

(set-refresh-dirs "src/dev" "src/clojure")

(defn start
  []
  (server/start))

(defn restart
  []
  (server/stop)
  (refresh :after 'user/start))

(comment
  (start)
  (stop))
