(ns cs-data-scrape.scrape
  (:require [clj-webdriver.taxi :as t]
            [net.cgrand.enlive-html :as e]
            [clojure.string :as str]
            [clj-time.core :as time]
            [clj-time.format :as timef]))

(def url-base "http://www.hltv.org")

(def url "http://www.hltv.org/matches/archive/")

(def formatter-match-date (timef/formatter "MMM dd, YYYY"))

(defn col-index [col-name head-map]
  (:column-num
   (first
    (filter #(= (:content %) col-name) head-map))))

(defn remove-from-col-that-start-with [col-beg col]
  "Removes entities from collections that begin with col-beg"
  (remove (fn [c] (re-find (re-pattern (str "^" col-beg)) (str c))) col))

(defn column-raw-data []
      (first
       (:content
        (first
         (e/select
          (e/html-snippet
           (t/html (t/find-element {:tag :div
                                    :id "matches"})))
          [:tbody])))))

(defn clean-single-match-data [raw-single-match header-data]
  (:content
   (nth
    (remove-from-col-that-start-with
     "\n "
     (:content
      ;TODO scrape nth instead of first available match
      raw-single-match))
    (col-index :Date header-data))))

(defn match-raw-data []
  (remove-from-col-that-start-with
   "\n "
   (rest
    (:content
     (first
      (e/select
       (e/html-snippet
        (t/html (t/find-element {:tag :div
                                 :id "matches"})))
       [:tbody]))))))

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

(defn filter-newer-than [seq date-field-fn iso-date]
  (let [iso-formatter (timef/formatter "YYYY-MM-dd")]
    (filter #(time/after? (date-field-fn %) (timef/parse iso-formatter iso-date)) seq)))

(defn scrape-matches-after! [iso-date]
  (do
    (t/set-driver! {:browser :firefox})
    (t/to url)
    ;Random wait 0-5s
    (loop [i 0
           result #{}]
      (let [result-filtered (set (filter-newer-than result :match-date iso-date))]
       (if (not (= result result-filtered))
         (do
           (println (str "Page " (+ i 1) " reached. Analysis done"))
           (t/close)
                                        ;Return result set at the end
           result-filtered)
         (do
                                        ;Wait 2-5s before scanning the page. Currently scrape has problems if the scan is done < 0.1s after the page load.
           (println (time (Thread/sleep (+ 2000 (rand 3000)))))
                                        ;Wait until matches-table has at least one match with link to the match-page (this is not working pefrectly, see the comment before wait.)
           (t/wait-until (t/exists? "div#matches>table>tbody>tr>td>a[href]"))
           (let [header-data
                 (thead-data-w-index
                  (thead-cols
                   (cleaned-thead-data
                    (column-raw-data))))
                 match-data (match-raw-data)
                 page-matches
                 (reduce
                  (fn [acc next]
                    (let [cleaned-match
                          (first (clean-single-match-data next header-data))]
                      (conj
                       acc
                       {:id (str/replace (:href (:attrs cleaned-match)) #"/match/"  "")
                        :link (str url-base (:href (:attrs cleaned-match)))
                                        ;Date stored as org.joda.time.DateTime.
                                        ;TBD if this should be converted to java.sql.Date in this phase or during database import
                        :match-date (timef/parse formatter-match-date (first (:content cleaned-match)))})))
                  []
                  match-data)]
             (println (t/find-element {:tag :a
                                       :text (+ i 1)}))
             (println "-----------------------------------------------")
             (t/wait-until (t/exists? {:tag :a
                                       :text (+ i 1)})
                                        ;Timeout 10s
                           10000)
                                        ;Dirty fix to button not clickable exception, scrolls page down
             (loop [j 0]
               (when-not
                   (try
                     (do
                       (t/click (t/find-element {:tag :a :href (str "javascript:nextPage(" (+ (* i 100) 100) ");")}))
                                        ;If click was successful (no exception) return true.
                       true)
                     (catch org.openqa.selenium.WebDriverException e
                       (do
                         (when (>= j 9)
                           (throw e))
                         (println "WebDriverException, scrolling down and trying again.")
                         (t/execute-script (str "window.scrollBy(0," 100 ")")))))
                 (recur (inc j))))
             (recur (inc i) (into result page-matches)))))))))

(comment 
;Test code snippets

  (def scrape-results (scrape-matches-after! "2016-06-30"))
  
  (println scrape-results)

  )
