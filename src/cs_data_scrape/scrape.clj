(ns cs-data-scrape.scrape
  (:require [clj-webdriver.taxi :as t]
            [net.cgrand.enlive-html :as e]
            [clojure.string :as str]))

(def url-base "http://www.hltv.org")

(def url "http://www.hltv.org/matches/archive/")


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
      ;TODO implement :id (remove /match/ from link)
      (print {:id (str/replace (:href (:attrs (first match-data))) #"/match/"  "")
              :link (str url-base (:href (:attrs (first match-data))))}))
    (t/close)))


(scrape-header!)

(scrape-match!)


(comment 
;WIP functions

  (thead-data-w-index (thead-cols (cleaned-thead-data (column-raw-data))))

(t/close)

(in-ns 'cs-data-scrape.scrape)

(print
 (:content
  ;TODO replace first based on column name
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
   0)))

url

(rand 5)

(print (rand 5))

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
