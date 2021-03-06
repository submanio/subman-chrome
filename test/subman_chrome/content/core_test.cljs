(ns subman-chrome.content.core-test
  (:require-macros [cemerick.cljs.test :refer [deftest is use-fixtures]]
                   [cljs.core.async.macros :refer [go]]
                   [clj-di.test :refer [with-fresh-dependencies]])
  (:require [cemerick.cljs.test]
            [cljs.core.async :refer [<!]]
            [clj-di.core :refer [register!]]
            [subman-chrome.content.core :as c]))

(deftype ExtensionMock [result-atom]
  Object
  (sendMessage [_ msg] (reset! result-atom msg)))

(defn clear!
  "Clear page."
  []
  (set! (.-innerHTML (.-body js/document))) "")

(use-fixtures :each (fn [f]
                      (with-fresh-dependencies
                        (clear!)
                        (f)
                        (clear!))))

(deftest test-get-titles
         (js/document.write "<a class='epinfo'>American Dad</a>
                             <a class='epinfo'>Family Guy</a>
                             <a class='detLink'>Simpsons</a>")
         (is (= (c/get-titles "epinfo") ["American Dad" "Family Guy"])))

(deftest test-with-menu?
         (js/document.write "<a id='with-menu' class='epinfo'></a>
                             <a id='without-menu' class='detLink'></a>")
         (is (true? (c/with-menu? (.getElementById js/document "with-menu")
                                  "epinfo")))
         (is (false? (c/with-menu? (.getElementById js/document "without-menu")
                                   "epinfo"))))
