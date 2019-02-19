(ns todo-backend.step-8
  "Step 8 - move JSON parsing and generating into it's own middleware"
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

;; Our first new function "updates" (actually creates a new one) the request
;; body. Originally the body is an inputstream (according the Ring spec). We use
;; the `comp` function here to create a new function which first slurps and than
;; parses the inputstream into Clojure data structures.

(comment
  (comp json/parse-string slurp)
  )

;; Than the `update` function takes the request and applies our parsing function
;; to the value of the `:body` key. Additionally we do the parsing only on POST
;; requests.

(defn parse-json [request]
  (if (= :post (:request-method request))
    (update request :body (comp json/parse-string slurp))
    request))

;; Next we use our `parse-json` function in a middleware which first parses the
;; body, calls the given handler with the new request and lastly renders the
;; response body as JSON.

(defn wrap-json [handler]
  (fn [request]
    (let [request (parse-json request)
          response (handler request)]
      (update response :body json/generate-string))))

(def todo-store (atom []))

;; Now with the `wrap-json` middleware in place, we can operate solely on
;; Clojure data in our handler, knowing the JSON I/O stuff will be handled for
;; us.

(defn root-handler [request]
  (case (:request-method request)
    :get
    {:status 200
     :body (deref todo-store)}

    :post
    (let [todo (:body request)]
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
