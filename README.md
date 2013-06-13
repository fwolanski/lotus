lotus
=====

A functional client-side web framework in clojurescript; along the lines of
[webfui](https://github.com/drcode/webfui), but with a slightly different
architecture. It is in very early beta stage, and is in no way ready for
any kind of production environment.

Install
=======

The documentation will come soon enough, but to see a demo, clone this repo,
complile with `lein deps && lein cljsbuild once`, and navigate to the
public folder with a modern web browser.

By default it will compile the first example (`core.cljs` located in the `src\examples\core`
folder). The contents of this file are show in the section below. The best
way to get familiar with this framework is to take a look at the `src\examples` folder,
and try each example one at a time by toggling the comments of each file's respective
`(bind-html-and-state : :)` line.

What does it look like?
=======================

As an example, here is a very simple page--the default one loaded from the examples
folder--that has a few divs and an input field. The
input field is bound to a state where a name entry is defined, and all the html is
defined as a function of the state. As the input field changes, so
does the state and the html.

```clojure
(ns examples.ex
  (:use-macros
   [lotus.macros :only [defhtml defstate]])
  (:use [lotus.dom :only [bind-html-and-state]]))


;; the state is only a name object
(defstate ex
  {:name "Filip" })

;; the html, which is updated dynamically when the state changes
(defhtml ex [s]
  [:div.container
   [:h1 "Hello " (:name s)]
   [:input {:bind-value :name}]
   (for [a (seq (:name s))]
     [:div a])])

(bind-html-and-state :ex :ex)
```

License
=======

Distributed under the
[Eclipse Public License 1.0](http://opensource.org/licenses/eclipse-1.0.php),
the same as Clojure.
