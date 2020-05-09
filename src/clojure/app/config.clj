(ns app.config)

(defn get-config
  [filename]
  (read-string (slurp filename)))

(def config (get-config "dev.secret.edn"))
