(ns todo-backend.step-9
  "Step 9 - sets up a new todo as initially not completed"
  (:require
    [cheshire.core :as json]
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

(defn parse-json [request]
  (if (= :post (:request-method request))
    (update request :body (comp json/parse-string slurp))
    request))

(defn wrap-json [handler]
  (fn [request]
    (let [request (parse-json request)
          response (handler request)]
      (update response :body json/generate-string))))

(def todo-store (atom []))

;; Initial todos should be not completed. So we just `assoc`-iate the key value
;; pair `:completed false` to the given todo.

(comment
  (assoc {} :completed false)
  )

(defn root-handler [request]
  (case (:request-method request)
    :get
    {:status 200
     :body (deref todo-store)}

    :post
    (let [todo (assoc (:body request) :completed false)]
      (swap! todo-store conj todo)
      {:status 201
       :body todo})

    :delete
    (do
      (reset! todo-store [])
      {:status 204})

    {:status 405}))

(comment
  (util/restart-server (wrap-json (wrap-cors-headers root-handler)))
  )
