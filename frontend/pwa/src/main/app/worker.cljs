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
       "/index.html"
       "/js/main/main.js"])

(.addEventListener
 js/self "install"
 (fn [event]
   (js/console.log "# Service worker is installed!")
   (.waitUntil event
               (p/let [cache (.open js/caches "static")]
                 (.addAll cache cache-files)))
   ))

(.addEventListener
 js/self "activate"
 (js/console.log "# Service worker is active!"))

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
