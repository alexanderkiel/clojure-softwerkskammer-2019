(ns todo-backend.step-4
  "Step 4 - the api root responds to a POST with the todo which was posted to it
            2/2"
  (:require
    [todo-backend.util :as util]))

;; Now we have duplicate code: the headers. To get rid of it we introduce
;; middleware which is perfect for such cross cutting concerns.

;; A middleware is a good example for an higher-order function. It takes a
;; function (a handler) and returns a function (also a handler). Here we use
;; an anonymous function (also called a lambda) to create a handler.

(comment
  (fn [request]
    body)
  )

;; A lambda can close over some values and so is called a closure. Here our
;; lambda closes over the `handler` function passed as argument to
;; `wrap-cors-headers`.

;; The handler function is used by the middleware to call-down the stack of
;; middlewares to the leave handler. The identity middleware looks like this.
;; It just calls the given handler with the original request.

(comment
  (defn identity-middleware [handler]
    (fn [request]
      (handler request)))
  )

;; With middleware in place, we can "modify" the request before calling the
;; inner handler and "modify" the response before returning it. Here we add our
;; headers to the response.

(defn wrap-cors-headers [handler]
  (fn [request]
    (assoc (handler request)
      :headers
      {"access-control-allow-origin" "*"
       "access-control-allow-headers" "Content-Type"})))

(defn root-handler [request]
  (case (:request-method request)
    :get
    {:status 200
     :body "Hello World"}

    :post
    {:status 201
     :body (slurp (:body request))}

    :options
    {:status 200}

    {:status 405}))

;; We have to apply our middleware by calling it with our `root-handler`,
;; creating a new handler which will add the headers in addition to what
;; `root-handler` already does.

(comment
  (util/restart-server (wrap-cors-headers root-handler))
  )
