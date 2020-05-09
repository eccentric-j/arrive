(ns mta.trains
  (:require
   [clj-http.client :as client])
  (:import
   (com.google.transit.realtime GtfsRealtime$FeedMessage NyctSubway)
   (com.google.protobuf ExtensionRegistry)))

(defn create-registry
  "Register the NYCT Protobuf extension.
  Returns a registry instance."
  []
  (doto
    (ExtensionRegistry/newInstance)
    NyctSubway/registerAllExtensions))

(defn bytestream->feed
  [feed-stream]
  (->> (GtfsRealtime$FeedMessage/parseFrom feed-stream (create-registry))))

(defn get-url
  [config]
  (format "%s?key=%s"
    (:url config)
    (:token config)))

(defn calc-arrival
  [arrival]
  (let [now (quot (System/currentTimeMillis) 1000)]
    (int (/ (- arrival now) 60))))

(defn parse-train
  [trip-update]
  (let [trip (.getTrip trip-update)
        train (.getRouteId trip)
        stops (.getStopTimeUpdateList trip-update)]
    (map
     #(hash-map
       :train train
       :station-id (.getStopId %)
       :arrives (calc-arrival (.getTime (.getArrival %))))
     stops)))

(defn fetch
  "
  Fetches trains and stations
  "
  [config]
  (->> (client/get (get-url config) {:as :byte-array})
       (:body)
       (bytestream->feed)
       (.getEntityList)
       (filter #(.hasTripUpdate %))
       (map #(.getTripUpdate %))
       (mapcat parse-train)))
