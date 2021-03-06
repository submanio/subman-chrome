(ns subman-chrome.background.models
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]
                   [clj-di.core :refer [defprotocol* let-deps]])
  (:require [cljs.core.async :refer [<! timeout]]
            [clojure.string :as string]
            [clojure.set :refer [map-invert]]
            [subman-chrome.shared.services.http :as http]
            [subman-chrome.shared.const :as const]))

(defn -get-sources
  "Get sources and repeat on error."
  []
  (go-loop []
    (let [response (<! (http/get* const/sources-url))]
      (if (= 200 (:status response))
        (into {} (for [[k v] (:body response)]
                   [k (string/lower-case v)]))
        (do (<! (timeout const/repeat-timeout))
            (recur))))))

(defn -get-source-id
  "Get source id by name."
  [name]
  (let-deps [sources :sources]
    (if (= name const/all-sources)
      const/all-sources-id
      ((map-invert sources) name))))

(defn -get-subtitles
  "Get subtitles for titles."
  [titles limit lang source]
  (go (->> (http/post* const/search-url
                       {:transit-params {:queries titles
                                         :limit limit
                                         :lang lang
                                         :source source}})
           <!
           :body)))

(defprotocol* models
  (get-sources [_])
  (get-source-id [_ name])
  (get-subtitles [_ titles limit lang source]))

(deftype models-impl []
  models
  (get-sources [_] (-get-sources))
  (get-source-id [_ name] (-get-source-id name))
  (get-subtitles [_ titles limit lang source] (-get-subtitles titles limit lang source)))
