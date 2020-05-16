(ns app.worker
  (:require
   [kitchen-async.promise :as p]))

(def cache-files
  #js ["/",
       "/css/style.css"
       "/images/icons-512.png"
       "/images/icons-192.png"
       "/images/icons-152.png"
       "/images/icons-144.png"
       "/images/logo.svg"
       "/index.html"
       "/js/main/main.js"
       "/fonts/noto-sans/noto-black.otf"
       "/fonts/noto-sans/noto-bold.otf"
       "/fonts/noto-sans/noto-light.otf"
       "/fonts/noto-sans/noto-regular.otf"])

(.addEventListener
 js/self "install"
 (fn [event]
   (js/console.log "# Service worker is installed!" event)
   (.waitUntil event
               (p/let [cache (.open js/caches "static")]
                 (.addAll cache cache-files)))
   ))

(.addEventListener
 js/self "activate"
 (fn [event]
   #_(.waitUntil event
               (p/let [keys (.keys js/caches)
                       keys (js->clj keys)]
                 (for [key keys]
                   (.delete js/caches key))))
   (js/console.log "# Service worker is active!")))

(.addEventListener
 js/self "fetch"
 (fn [event]
   (.respondWith
    event
    (p/let [req (.-request event)
            res (.match js/caches req)]
      (if res
        res
        (js/fetch req))))))

(defn init
  []
  (js/console.log "# I'm workin' over here"))
