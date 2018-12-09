# cs-data-scrape

## Description

Project that was used to scape Counter Strike match results from hltv.org. 

The scraped match results were saved to SQL RMBDS database, the data model was designed so that it could be easily analyzed with SQL queries (see sql/).

The project was originally made in 2016. As expected, hltv.org website structure has now changed and this scraper do not work anymore.

Project uses Clojure library [clj-webdriver](https://github.com/semperos/clj-webdriver) that is Clojure wrapper for [Selenium Webdriver](https://www.seleniumhq.org/). Unfortunately, clj-webdirver is now unmaintained and do not work with latest versions of Selnium and Firefox. Therefore, reviving this project would require to change how Selnium is used.

Nevertheless, was fun would do it again =).

## Installation

Rename resources/database_info.xml.dist to -> database_info.xml and fill in the database connection info.

Project was tested with Postgresql, but should work with most of the SQL RMDBS.

This project uses leiningen:

```bash
cd cs-data-scrape
lein deps
lein run
```

## Usage

Was used manually from REPL, see e.g. (core/-main).

## Bugs

Don't work anymore (sadface), see the Description.

Some special case mathed do not work correctly and need to be excluded manually, see core/non-valid-match-ids.

## License

Copyright © 2016 tkasu

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
