(ns mala.component
  (:require [com.stuartsierra.component :as component]
            [taoensso.timbre :as log]))

(comment
  (defn process-message
    [state from to data]
    (let [mime-message (javax.mail.internet.MimeMessage.
                        (javax.mail.Session/getDefaultInstance
                         (java.util.Properties.)) data)
          message {:from from
                   :to to
                   :time (System/currentTimeMillis)
                   :subject (.getSubject mime-message)
                   :content (.getContent mime-message)}]
      (swap! state (fn [state] (update state to #(conj % message))))))


  (defn message-listener [state]
    (reify org.subethamail.smtp.helper.SimpleMessageListener
      (accept [this from recipient]
        true)
      (deliver [state this from to data]
        (process-message from to data))))

  (defn




    )
  (def smtp-server (org.subethamail.smtp.server.SMTPServer.
                    (org.subethamail.smtp.helper.SimpleMessageListenerAdapter.
                     (message-listener)))))

(defn- already-started
  [{:keys [port] :as service}]
  (log/info (str "Mala service already started on port " port "."))
  service)

(defn- start-service
  [{:keys [port routes] :as service} routes]
  (log/info (str "Starting Mala on port " port "..."))
  (try
    (let [server (doto (org.subethamail.wiser.Wiser.)
                   (.setPort port)
                   (.start))]
      (log/info (str "Finished starting."))
      (assoc service :server server))
    (catch java.net.BindException e
      (throw (ex-info (str "Port " port " is already in use.") {:port port})))))

(defn- stop-service
  [{:keys [port server] :as service}]
  (log/info (str "Stopping Mala on port " port "..."))
  (.stop server)
  (dissoc service :server))

(defn- already-stopped
  [{:keys [id] :as service}]
  (log/info (str "Mala already stopped."))
  service)

(defrecord MalaService [port server]
  component/Lifecycle
  (start [this]
    (if server
      (already-started this)
      (start-service this)))
  (stop [this]
    (if server
      (stop-service this)
      (already-stopped this))))

(defn mala-service
  [{:keys [mala-port] :as config}]
  {:pre [(integer? mala-port)
         (> mala-port 0)]}
  (map->MalaService {:port mala-port}))
