(ns todo-backend.step-6
  "Step 6 - after a DELETE the api root responds to a GET with a JSON
            representation of an empty array"
  (:require
    [todo-backend.util :as util]))

(defn wrap-cors-headers [handler]
  (fn [request]
    (if (= :options (:request-method request))
      {:status 200
       :headers
       {"access-control-allow-origin" "*"
        "access-control-allow-headers" "Content-Type"
        "access-control-allow-methods" "DELETE"}}
      (assoc (handler request)
        :headers
        {"access-control-allow-origin" "*"
         "access-control-allow-headers" "Content-Type"}))))

;; We'll just return an empty JSON array here.

(defn root-handler [request]
  (case (:request-method request)
    :get
    {:status 200
     :body "[]"}

    :post
    {:status 201
     :body (slurp (:body request))}

    :delete
    {:status 204}

    {:status 405}))

(comment
  (util/restart-server (wrap-cors-headers root-handler))
  )
