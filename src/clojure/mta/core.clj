(ns mta.core
  (:require
   [clojure.string :as s]
   [mta.stations :as stations]
   [mta.trains :as trains]))

(defn assoc-station
  [stations train]
  (let [train-station (:station-id train)
        id (s/join "" (butlast train-station))
        direction (str (last train-station))
        station (get stations id {})]
    (merge train {:station station
                  :direction direction})))

(defn train-arrivals
  [config]
  (let [trains (trains/fetch config)
        stations (stations/fetch config)]
    (->> trains
         (map (partial assoc-station stations)))))

(defn -main
  []
  (->> (train-arrivals)))

(comment
  (-main))
