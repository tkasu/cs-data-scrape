(ns cs-data-scrape.scrape
  (:require [clj-webdriver.taxi :as t]
            [net.cgrand.enlive-html :as e]
            [clojure.string :as str]
            [clj-time.format :as timef]))

(def url-base "http://www.hltv.org")

(def url "http://www.hltv.org/matches/archive/")

(def formatter-match-date (timef/formatter "MMM dd, YYYY"))

(defn col-index [col-name head-map]
  (:column-num
   (first
    (filter #(= (:content %) col-name) head-map))))

(defn column-raw-data []
      (first
       (:content
        (first
         (e/select
          (e/html-snippet
           (t/html (t/find-element {:tag :div
                                    :id "matches"})))
          [:tbody])))))

(defn remove-from-col-that-start-with [col-beg col]
  "Removes entities from collections that begin with col-beg"
  (remove (fn [c] (re-find (re-pattern (str "^" col-beg)) (str c))) col))

(defn cleaned-thead-data [col-raw]
  "Cleanup for raw column data."
  (filter
   #(= (:tag %) :td)
   (remove-from-col-that-start-with "\n  " (:content col-raw))))

(defn thead-cols [thead-data]
  "Gets hltv.org header data and creates a seq of keyword from it."
  (reduce (fn [cum next]
            (let [thead-keyword (keyword (first (:content next)))]
              (conj cum {:content thead-keyword})))
          []
          thead-data))

(defn thead-data-w-index [thead-col-data]
  "Adds column number to thead-data. Should be called to collection after function thead-cols."
  (reduce
   #(conj %1 (assoc %2 :column-num (.indexOf thead-col-data %2)))
   []
   thead-col-data))

(defn scrape-header! []
  (do
    (t/set-driver! {:browser :firefox})
    (t/to url)
    ;Random wait 0-5s
    (Thread/sleep (rand 5000))
    (t/wait-until (t/exists? {:tag :div
                              :id "matches"}))
    (println
     (thead-data-w-index
      (thead-cols
       (cleaned-thead-data
        (column-raw-data)))))
    (t/close)))

(defn scrape-match! []
  (do
    (t/set-driver! {:browser :firefox})
    (t/to url)
    ;Random wait 0-5s
    (Thread/sleep (rand 5000))
    (t/wait-until (t/exists? {:tag :div
                              :id "matches"}))
    (let [header-data
          (thead-data-w-index
           (thead-cols
            (cleaned-thead-data
             (column-raw-data))))
          match-data
          (:content
           (nth
            (remove-from-col-that-start-with
             "\n "
             (:content
              (first
               (remove-from-col-that-start-with "\n  "
                                                (rest
                                                 (:content
                                                  (first
                                                   (e/select
                                                    (e/html-snippet
                                                     (t/html (t/find-element {:tag :div
                                                                              :id "matches"})))
                                                    [:tbody]))))))))
            (col-index :Date header-data)))]
      (print {:id (str/replace (:href (:attrs (first match-data))) #"/match/"  "")
              :link (str url-base (:href (:attrs (first match-data))))
              ;Date stored as org.joda.time.DateTime.
              ;TBD if this should be converted to java.sql.Date in this phase or during database import
              :match-date (timef/parse formatter-match-date (first (:content (first match-data))))}))
    (t/close)))

(comment 
;Test code snippets

  (scrape-header!)

  (scrape-match!)

  )
