(ns mta.stations
  (:require [clj-http.client :as client]
            [clojure.string :as str]
            [clojure.pprint :refer [pprint]]))

(def station-keys {:id 2
                   :line 4
                   :name 5
                   :latitude 9
                   :longitude 10})

(def station-parsers {:latitude #(BigDecimal. %)
                      :longitude #(BigDecimal. %)})

(defn get-stations-csv!
  "
  Requests the list of stations live from the mta.
  Returns a lazy seq of CSV rows.
  "
  [url]
  (let [csv-str (->> (client/get url) :body)]
    (str/split csv-str #"\r\n")))

(defn parse-station-value
  "
  Looks up a parser for the given station key and applies it to the given
  value.
  Takes a keyword key and any value type to format.
  Returns a parsed value."
  [key value]
  (let [parse-fn (get station-parsers key identity)]
    (parse-fn value)))

(defn csv->station
  "
  Converts a list of csv row string and parses specified columns into a map
  with named keys.
  "
  [station-csv-row]
  (let [values (str/split station-csv-row #",")]
    (into {} (map (fn
                    [[key i]]
                    [key (parse-station-value key (get values i))])
                  station-keys))))

(defn index-by-id
  "
  Update the map with an id value to station for fast lookups
  "
  [m station]
  (assoc m (str (:id station)) station))

(defn fetch
  "Returns a vector of station hash maps."
  [config]
  (->> (get-stations-csv! (:stations-url config))
       (drop 1)
       (map csv->station)
       (reduce index-by-id {})))

(comment)
