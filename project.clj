(defproject lotus "0.1.0-SNAPSHOT"
  :description "A functional client-side web framework"
  :url "https://github.com/fwolanski/lotus"
  :license {
    :name "Eclipse Public License"
    :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [com.cemerick/piggieback "0.0.4"]]
  :plugins [[lein-cljsbuild "0.3.0"]]
  :hooks [leiningen.cljsbuild]
  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
  :cljsbuild {
    :builds [{
      :source-paths ["src"]
      :compiler {
        :optimizations :whitespace
        :pretty-print true
        ;; :jar true
        :output-to "public/lotus.js"}}
     {
      :source-paths ["test"]
      :compiler {
        :optimizations :whitespace
        :pretty-print true
        :output-to "public/lotus-test.js"}}]
    :test-commands {
      :unit ["phantomjs" "public/unit-test.js" "public/index-test.html"]}})

;; (cemerick.piggieback/cljs-repl)
