(ns app.client
  (:require
   [reagent.core :as r]
   [reagent.dom :as rdom]
   [clojure.string :as str]
   [kitchen-async.promise :as p]
   [cljs.pprint :refer [pprint]]))

(defonce trains (r/atom []))
(defonce active-train (r/atom nil))
(defonce location (r/atom {}))

(defn group-by-trains
  [stops]
  (group-by :train/name stops))

(defn group-by-stations
  [stops-by-train]
  (->> stops-by-train
       (map (fn [[train stops]]
              [train
               (->> stops
                    (group-by #(get-in % [:train/station :station/name]))
                    (sort-by first))]))))
(defn hyp
  [dx dy]
  (-> (+ (* dx dx)
         (* dy dy))
      (js/Math.abs)
      (js/Math.sqrt)))

(defn line->station
  [train]
  (-> train second first second first :train/station))

(defn stations->station
  [train]
  (-> train second first :train/station))

(defn nearest-station
  [you station]
  (let [your-lat (:lat you)
        your-lng (:lng you)
        station-lat (:station/latitude station)
        station-lng (:station/longitude station)
        dx (- station-lat your-lat)
        dy (- station-lng your-lng)
        d (hyp dx dy)]
    (println (:station/name station ) "distance: " d)
    d))

(defn sort-by-nearest
  [coords f items]
  (sort-by #(nearest-station coords (f %)) items))


(defn group-arrivals
  [arrivals]
  (->> arrivals
       (filter #(pos? (:train/arrives %)))
       (sort-by :train/arrives)
       (group-by-trains)
       (group-by-stations)
       (sort-by first)))

(defn fetch-arrivals
  []
  (p/let [arrivals (p/-> (js/fetch "/api/trains")
                         (.json)
                         (js->clj :keywordize-keys true))]
    (pprint (first arrivals))
    (reset! trains (-> arrivals
                       (group-arrivals)))))

(defn direction
  [stop]
  (if (= (:train/direction stop) "N")
    "Uptown"
    "Downtown"))

(defn train-stops
  [stops]
  [:ul.stops
   (for [[index stop] (take 3 (map-indexed vector stops))]
     [:li.stop
      {:key index}
      [:span.stop__arrives
       (str (direction stop) " arrives in " (:train/arrives stop) " minutes")]])])

(defn train-station
  [_ station stops]
  (let [is-open (r/atom false)]
    (fn []
      [:li.station
       [:button.station__name
        {:on-click (fn [_] (swap! is-open not))}
        (str station
             " [" (if @is-open "-" "+") "]")]
       (when @is-open
         [train-stops stops])])))

(defn train-stations
  [{:keys [train]} stations]
  [:div.stations
   [:h3.stations__train (str train " train")]
   [:ul.stations__list
    (for [[index [station stops]] (map-indexed vector stations)]
      [train-station {:key index} station stops])]])

(defn toggle-train
  [train-name]
  (if (= @active-train train-name)
    (reset! active-train nil)
    (reset! active-train train-name)))

(defn train-line
  [{:keys [key name is-active]}]
  [:div.train
   {:class (when is-active "train--is_active")}
   [:button.train__name
    {:class (str "train--name_" name)
     :on-click #(toggle-train name)}
    name]])

(defn arrivals
  [{:keys [coords]} trains]
  (let [active @active-train
        trains (if (empty? coords)
                 trains
                 (sort-by-nearest coords line->station trains))]
    [:div.arrivals
     [:div.trains
      (for [[train stations] trains]
        [train-line {:key train
                     :name train
                     :is-active (= active train)}])]
     [:div.stations
      (for [[train stations] trains]
        (when (= active train)
          [train-stations
           {:key (str train "-stations")
            :train train}
           (if (empty? coords)
             stations
             (sort-by-nearest coords stations->station stations))]))]]))

(defn simple-example
  []
  [:div.app
   [:header.t-center
    [:img.logo {:src "/images/logo.svg" :alt "Arrive"
                :width "100px"}]
    [:h1.t-center
     "Arrive"]]
   (when-let [trains @trains]
     [arrivals {:coords @location} trains])
   [:div.actions.t-center
    [:button.btn
     {:on-click fetch-arrivals}
     "Fetch Arrival Times"]]])


(defn update-location
  [position]
  (let [lat (.. position -coords -latitude)
        lng (.. position -coords -longitude)]
    (reset! location {:lat lat :lng lng})))

(defn get-location
  [geo]
  (js/Promise.
   (fn [resolve reject]
     (.getCurrentPosition geo resolve reject))))

(defn geolocation
  []
  (when-let [geo (some-> js/window (.-navigator) (.-geolocation))]
    (p/-> geo
          (get-location)
          (update-location))))

(defn watch-location
  []
  (add-watch
   location :geo
   (fn [key ref prev next]
     (println "Coords:" next))))

(defn ^:export init
  ""
  []
  (set! (.-onload js/window)
        (fn []
          (when-let [service-worker (.-serviceWorker js/navigator)]
            (.register service-worker "./worker.js" ))
          (fetch-arrivals)))
  (watch-location)
  (geolocation)
  (rdom/render [simple-example] (js/document.getElementById "app"))
  (js/console.log "Loaded"))

(defn ^:export refresh
  []
  (rdom/render [simple-example] (js/document.getElementById "app"))
  (js/console.log "Refresh"))
