(ns todo-backend.step-3
  "Step 3 - the api root responds to a POST with the todo which was posted to it
            1/2"
  (:require
    [todo-backend.util :as util]))

;; Now we have to differentiate between GET, POST and OPTION requests.

;; The request is a map with at least an `:request-method` key were the values
;; are the HTTP methods as lower-case keywords.

;; We'll use the `case` macro to test for the individual methods. The `case`
;; macro takes an expression (`(:request-method request)` in this case), a set
;; of clauses and an optional default expression. A clause consists of a test
;; constant and a result expression. Test constants used are `:get`, `:post`
;; and `:options`. The expression `(:request-method request)` is a function call
;; were the function is the keyword `:request-method` and the argument is the
;; `request` map. Normally functions have to be defined and are referred by
;; their name which is a symbol. Here the keyword itself is also a function.
;; Its a function which gets the value of the keyword out of a map.

;; In the body of the POST response, we just output the request body. The
;; `slurp` function reads a string from an inputstream.

(defn root-handler [request]
  (case (:request-method request)
    :get
    {:status 200
     :headers
     {"access-control-allow-origin" "*"
      "access-control-allow-headers" "Content-Type"}
     :body "Hello World"}

    :post
    {:status 201
     :headers
     {"access-control-allow-origin" "*"
      "access-control-allow-headers" "Content-Type"}
     :body (slurp (:body request))}

    :options
    {:status 200
     :headers
     {"access-control-allow-origin" "*"
      "access-control-allow-headers" "Content-Type"}}

    {:status 405}))

(comment
  (util/restart-server root-handler)
  )
