(ns cs-data-scrape.webdriver-scrape
  (:require [clj-webdriver.taxi :as t]
            [net.cgrand.enlive-html :as e]
            [clojure.string :as str]))

(def url "http://www.hltv.org/matches/archive/")

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

(defn scrape! []
  (do
    (t/set-driver! {:browser :firefox})
    (t/to url)
    ;Random wait 0-5s
    (Thread/sleep (rand 5000))
    (t/wait-until (t/exists? {:tag :div
                              :id "matches"}))
    (clojure.pprint/pprint
     (thead-data-w-index
      (thead-cols
       (cleaned-thead-data
        (column-raw-data)))))
    (t/close)))

(scrape!)


(comment 
;WIP functions

  (thead-data-w-index (thead-cols (cleaned-thead-data (column-raw-data))))

(t/close)

(rand 5)

(time (Thread/sleep (rand 5000)))

(t/set-driver! {:browser :firefox})

(t/to url)

(t/exists? {:tag :div
            :id "matches"})

(e/select
 (e/html-snippet
  (t/html (t/find-element {:tag :div
                           :id "matches"})))
 [:tbody])

 (clojure.pprint/pprint (first
                         (:content
                          (first
                           (e/select
                            (e/html-snippet
                             (t/html (t/find-element {:tag :div
                                                      :id "matches"})))
                            [:tbody])))))

(println (+ 4 4 ))

 (def col-raw (first
               (:content
                (first
                 (e/select
                  (e/html-snippet
                   (t/html (t/find-element {:tag :div
                                            :id "matches"})))
                  [:tbody])))))

 (remove #(clojure.string/includes? % "\n  ") (:content col-raw))

 (def thead-data
   (filter
    #(= (:tag %) :td)
    (remove-from-col-that-start-with "\n  " (:content col-raw))))

 thead-data

 (first thead-data)

 (rest thead-data)

 (reduce #(conj %1 (keyword (first (:content %2)))) [] thead-data)

 (def thead-keywords
   (reduce #(conj %1 (keyword (first (:content %2)))) [] thead-data))

thead-keywords

(.indexOf thead-keywords :Teams)


 (thead-cols thead-data)

 (thead-data-w-index (thead-cols thead-data))


  (+ 2 2)

  (+ 2 2)

  (def test-str (str "^" "\n "))

  (re-find (re-pattern (str "^" "\n  ")) "\n  loloo")

)
