(ns mala.system
  (:require [taoensso.timbre :as log]
            [yada.yada :as yada]
            [yada-component.core :as yada-component]))

(defn routes
  []
  ["" [["/hello" (yada/resource
                   {:access-control {:allow-origin "*"}
                    :methods
                    {:get
                     {:produces
                      {:media-type #{"application/edn" "application/json"}
                       :language #{"en"}}
                      :response (fn [request]
                                  (log/info "Saying hello!")
                                  "Hello!")}}})]
       [true (yada/handler nil)]]])

(defn system
  [config]
  {:app (yada-component/yada-service config (routes))})
