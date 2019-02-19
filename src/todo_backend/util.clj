(ns todo-backend.util
  (:require
    [aleph.http :as http]))

(def server nil)

(defn- start-server [handler]
  (http/start-server handler {:port 8080}))

(defn restart-server [handler]
  (when server
    (.close server))

  (alter-var-root #'server (constantly (start-server handler))))
