(ns mala.system-test
  (:require [clojure.test :refer :all]
            [com.stuartsierra.component :as component]
            [clj-http.client :as http]
            [mala.system :as system]
            [clojure.data.json :as json]))

(def config {:id "mala" :port 8081})

(defmacro with-system
  [& body]
  `(let [~'system (component/start-system (system/system config))]
     (try
       ~@body
       (finally (component/stop-system ~'system)))))

(deftest saying-hello
  (with-system
    (testing "should say hello in English by default"
      (let [{:keys [status body]} (http/get "http://localhost:8081/api/hello")]
        (is (= 200 status))
        (is (= "Hello, world!\n" body))))))
