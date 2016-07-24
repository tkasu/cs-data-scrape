(defproject cs-data-scrape "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
 :dependencies [[org.clojure/clojure "1.8.0"]
                [org.clojure/java.jdbc "0.3.6"]
                [postgresql/postgresql "8.4-702.jdbc4"]
                [clojure-csv/clojure-csv "2.0.1"]
                [enlive "1.1.5"]
                [clj-http "2.2.0"]
                [clj-webdriver "0.7.2"]
                [org.seleniumhq.selenium/selenium-java "2.52.0"]]
  :main ^:skip-aot cs-data-scrape.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
