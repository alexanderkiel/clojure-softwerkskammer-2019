(ns todo-backend.step-7
  "Step 7 - adds a new todo to the list of todos at the root url"
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

;; We store the todos in a Clojure atom. https://clojure.org/reference/atoms

;; A atom is one way to handle state in Clojure. Up until now, our root-handler
;; was just a pure function. It had only the request as input and calculated the
;; response accordingly. Now we need to make side-effects in order to store and
;; delete todos. Side-effects are not bad - we need them. But it's important to
;; handle them in a controlled way.

;; The core function `atom` takes an initial value and creates a box with a
;; pointer to that initial value.

(def todo-store (atom []))

;; +------+
;; | atom |--> []
;; +------+

;; We can dereference the atom to get the value it points to.

(comment
  (deref todo-store)
  )

;; We can reset the atom to a new value. `deref` will now return the new value.
;; The function `reset!` ends with an explanation mark by convention because it
;; causes a side-effect to happen. So `reset!` is impure.

(comment
  (reset! todo-store 1)
  )

;; We can also swap the atom. Swapping uses a function which is called with the
;; old value to calculate the new value. Clojure uses CAS (compare and swap) to
;; allow for concurrent updates. The function given to swap has to be pure
;; because it can be called multiple times for one update in case of congestion.
;; We can implement a concurrent counter using `swap!` and `inc`.

(comment
  (swap! todo-store inc)
  )

;; Because we have to return a list of todos and we don't like to use string
;; concatenation, we actually have to parse the json into generic Clojure data
;; structures like maps and lists in order to be able to output a list of them
;; as JSON again. The two functions `parse-string` and `generate-string` just do
;; exactly that. JSON objects become Clojure maps, JSON Arrays Clojure Lists,
;; strings, numbers and booleans remain.

(defn root-handler [request]
  (case (:request-method request)
    :get
    {:status 200
     :body (json/generate-string (deref todo-store))}

    :post
    (let [todo (json/parse-string (slurp (:body request)))]
      (swap! todo-store conj todo)
      {:status 201
       :body (json/generate-string todo)})

    :delete
    (do
      (reset! todo-store [])
      {:status 204})

    {:status 405}))

(comment
  (util/restart-server (wrap-cors-headers root-handler))
  )
