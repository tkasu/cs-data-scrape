(ns cs-data-scrape.scrape
  (:require [clj-webdriver.taxi :as t]
            [net.cgrand.enlive-html :as e]
            [clojure.string :as str]
            [clj-time.core :as time]
            [clj-time.format :as timef]))

(def url-base "http://www.hltv.org")

(def url "http://www.hltv.org/matches/archive/")

(def test-match-url "http://www.hltv.org/match/2303771-kalashnikov-47-archangels-esea-premier-season-22-europe")

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
         (e/html-snippet
          (t/html (t/find-element {:css "div#matches>table>tbody"})))))))

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
      (e/html-snippet
       (t/html (t/find-element {:css "div#matches>table>tbody"}))))))))

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

 (defn match-general-data []
   (let [teams (->
                {:xpath "//span[starts-with(@class,'matchScore')]/.."}
                t/find-elements )
         team1-raw (t/text (first teams))
         team2-raw (t/text (first (rest teams)))
         team1-name (as-> team1-raw v
                          (str/split v #"\s")
                          (drop-last v)
                          (str/join " " v))
         team2-name (as-> team2-raw v
                          (str/split v #"\s")
                          (rest v)
                          (str/join " " v))
         team1-score (-> team1-raw
                         (str/split #"\s")
                         last
                         Integer.)
         team2-score (-> team2-raw
                         (str/split #"\s")
                         first
                         Integer.)]
     {:team1-name team1-name
      :team1-score team1-score
      :team1-result (cond
                     (> team1-score team2-score) "W"
                     (< team1-score team2-score) "L"
                     (= team1-score team2-score) "D"
                     :else :error)
      :team2-name team2-name
      :team2-score team2-score
      :team2-result (cond
                     (> team2-score team1-score) "W"
                     (< team2-score team1-score) "L"
                     (= team2-score team1-score) "D"
                     :else :error)}))

(defn match-votes []
   (let [raw-html
         (->>
                                        ;xpath to parent div-element of 'Veto process textbox'
          {:xpath "//div[text()='Veto process']/.."}
          t/find-element
          t/html
          e/html-snippet
          first
          :content
          (remove-from-col-that-start-with "\n  "))
         vote-html (rest raw-html)]
     (->>
      (reduce #(conj %1 (first (:content %2)))  #{} vote-html)
      (remove nil?))))

(defn parse-map-result [span-parent-div-elem]
 (let [res-span (-> span-parent-div-elem
                    t/xpath
                    (str "/span"))]
   (when (t/exists? {:xpath res-span})
     (let [span-elems (t/find-elements {:xpath res-span})] 
       {:team1-score (-> (nth span-elems 0)
                         (t/attribute :text)
                         Integer.)
        :team2-score (-> (nth span-elems 1)
                         (t/attribute :text)
                         Integer.)
        :team1-r1-score (-> (nth span-elems 2)
                            (t/attribute :text)
                            Integer.)
        :team1-r1-side (-> (nth span-elems 2)
                           (t/attribute :style)
                           team-by-css-style)
        :team2-r1-score (-> (nth span-elems 3)
                            (t/attribute :text)
                            Integer.)
        :team2-r1-side (-> (nth span-elems 3)
                           (t/attribute :style)
                           team-by-css-style)
        :team1-r2-score (-> (nth span-elems 4)
                            (t/attribute :text)
                            Integer.)
        :team1-r2-side (-> (nth span-elems 4)
                           (t/attribute :style)
                           team-by-css-style)
        :team2-r2-score (-> (nth span-elems 5)
                            (t/attribute :text)
                            Integer.)
        :team2-r2-side (-> (nth span-elems 5)
                           (t/attribute :style)
                           team-by-css-style)}))))

(defn team-by-css-style [css-style]
  (cond
   (= css-style "color: red;") "T"
   (= css-style "color: blue;") "CT"
   :else :error))

  (defn get-child-img-src [elem]
    (let [child-img-xpath
          (-> elem
              t/xpath
              (str "/img"))]
      (when (t/exists? {:xpath child-img-xpath})
       (-> (t/find-element {:xpath child-img-xpath})
           (t/attribute :src)))))

(defn match-results-for-maps [] 
  (let [elements (t/find-elements {:xpath "//div[@class='hotmatchboxheader']/div[starts-with(text(),'Maps')]/../../div[@class='hotmatchbox']/div"})
        next-map (atom nil)
        index (atom 0)
        results (atom [])]
    (loop [elems elements]
      (if (empty? elems)
        @results
        (do
          (if @next-map
            (reset! results (conj @results {:map @next-map
                                            :map-results (parse-map-result (first elems))})))
          (reset! next-map (get-child-img-src (first elems)))
          (println (str @next-map " " @index))
          (reset! index (inc @index))
          (recur (rest elems)))))))

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

(defn scrape-match! [url]
  (t/set-driver! {:browser :firefox})
  (t/to url)
  (do
                                        ;Random wait
    (println (time (Thread/sleep (+ 2000 (rand 3000)))))
                                        ;Wait Veto Process vote box is found
    (t/wait-until (t/exists? {:xpath "//div[text()='Veto process']"}))
    (let [votes (match-votes)
          gen-data (match-general-data)
          map-results (match-results-for-maps)]
      (t/close)
      {:id url
       :gen-info gen-data
       :map-results map-results
       :votes votes})))

(comment 
;Test code snippets
  test-match-url
  
  (t/to test-match-url)
  
  (spit "test-match.txt" (scrape-match! test-match-url))

  (def t-elems (t/find-elements {:xpath "//div[@class='hotmatchboxheader']/div[starts-with(text(),'Maps')]/../../div[@class='hotmatchbox']/div"}))

  (def scrape-results (scrape-matches-after! "2016-06-30"))
  
  (println scrape-results)

  )
