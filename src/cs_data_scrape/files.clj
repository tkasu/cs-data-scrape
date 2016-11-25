(ns cs-data-scrape.files
  (:require 
    [clojure.java.io :as io]
   [net.cgrand.enlive-html :as e])
  (:gen-class))

(defn read-xml [xml-file tag-key]
  (let [xml-res
        (with-open [rdr (io/reader (.getPath (io/resource xml-file)))]
          (e/xml-resource rdr))]
    (first (apply :content (e/select xml-res [tag-key])))))
