(ns mta.trains
  (:require
   [clj-http.client :as client]
   [clojure.pprint :refer [pprint]])
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
        id (.getTripId trip)
        stops (.getStopTimeUpdateList trip-update)
        ny-trip (.getExtension trip NyctSubway/nyctTripDescriptor)]
    (map
     #(hash-map
       :train/id id
       :train/name train
       :train/station-id (.getStopId %)
       :train/arrives (calc-arrival (.getTime (.getArrival %)))
       :train/is-assigned (.getIsAssigned ny-trip))
     stops)))

(defn fetch
  "
  Fetches trains and stations
  "
  [config]
  (let [url (get-url config)]
    (->> (client/get url {:as :byte-array})
         (:body)
         (bytestream->feed)
         (.getEntityList)
         (filter #(.hasTripUpdate %))
         (map #(.getTripUpdate %))
         (mapcat parse-train)
         (filter :train/is-assigned))))
