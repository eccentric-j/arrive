(ns mta.core
  (:require
   [clojure.string :as s]
   [mta.stations :as stations]
   [mta.trains :as trains]))

(defn get-config
  [filename]
  (read-string (slurp filename)))

(defn assoc-station
  [stations train]
  (let [train-station (:station-id train)
        id (s/join "" (butlast train-station))
        direction (str (last train-station))
        station (get stations id {})]
    (merge train {:station station
                  :direction direction})))

(defn -main
  []
  (let [config (get-config "dev.secret.edn")
        trains (trains/fetch config)
        stations (stations/fetch config)]
    (->> trains
         (map (partial assoc-station stations)))))

(comment
  (-main))
