(ns todo-backend.step-5
  "Step 5 - the api root responds successfully to a DELETE"
  (:require
    [todo-backend.util :as util]))

;; Here we move the handling of all OPTIONS requests into the CORS middleware
;; because OPTIONS requests are only about CORS headers in our case. The
;; advantage is that we don't have to handle the OPTIONS case in our handlers
;; anymore. Because we have only two cases here, we will use the `if` special
;; form instead of the `case` macro.

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

;; In the DELETE case, we just return a 204.

(defn root-handler [request]
  (case (:request-method request)
    :get
    {:status 200
     :body "Hello World"}

    :post
    {:status 201
     :body (slurp (:body request))}

    :delete
    {:status 204}

    {:status 405}))

(comment
  (util/restart-server (wrap-cors-headers root-handler))
  )
