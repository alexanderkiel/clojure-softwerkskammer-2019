(ns todo-backend.step-1
  "Step 1 - Create a webserver returning just Hello."
  (:require
    [aleph.http :as http]))

;; We have a namespace here. Every file is a namespace on its own. It's just a
;; way to organize functions. No module, no exports. Like a Java package but in
;; a single file.

;; The first form in every namespace is the `ns` form. Forms consist of Clojure
;; data structures. Here we have a list with two symbols, followed by a string
;; and another list.

(comment
  (ns todo-backend.step-1 "" ())
  )

;; Lists are an integral part of every LISP (List Processing) and so also of
;; Clojure. The idea is to have a common syntax for everything. So everything is
;; a list were the first element is a or names a function or macro and the rest
;; of the elements are the arguments to that function. Lists are evaluated by
;; calling the function with its arguments were arguments are evaluated first.
;; Macros look like functions but are evaluated at compile time and there
;; arguments are not evaluated first.

;; Here `ns` is a symbol naming the built-in macro ns. Symbols are like
;; identifiers in other languages. The arguments of the ns-macro can be queried
;; by calling `doc`:

(comment
  (clojure.repl/doc ns)
  )

;; It returns the argument vector `[name docstring? references*]`. A
;; vector something like a list for now. The argument vector says that the ns
;; macro needs a name, followed by an optional docstring, and zero or more
;; references. We have the name `todo-backend.step-1`, a docstring and one
;; reference.

;; The reference we use is used to refer to other namespaces. It's form is:

(comment
  (:require [aleph.http :as http])
  )

;; Here we also have a list consisting of a keyword and a vector. A keyword
;; starts with a colon and is different from a symbol as it just refers to
;; itself. So it's not a name for something else. The list here doesn't
;; represent a function call. It's just a list the ns-macro takes as an
;; argument because macros get there arguments unevaluated. With this macros
;; ca be used to build semantic structures which are not like function calls.

;; The next form is a function definition. It registers a function by its name
;; `root-handler` hat can be evaluated later. This function is our first simple
;; handler for our webserver.

(defn root-handler [request]
  {:status 200
   :body "Hello"})

;; Like the ns-macro, `defn` is also a macro. The argument vector is the
;; following: `[name doc-string? [params*] body]`. So `root-handler` is the name
;; of our function and `[request]` is the argument vector consisting of a single
;; argument `request`. The body is the map `{:status 200 :body "Hello"}`. Maps
;; are like Java maps or dictionaries in other languages. They consist of
;; key-value pairs. In our function, we just ignore the `request` argument and
;; return the map.

(comment
  (root-handler nil)
  )

;; In Clojure there is a specification defining the shape of requests and
;; responses called Ring. There a response is a map with well defined key-value
;; pairs. The most basic keys are `:status` and `:body`.

;; With our handler in place, we can start a webserver passing our handler by
;; referring it by its name with is the symbol `root-handler`. Passing a symbol
;; as argument to a function first resolves it to the thing it refers to. In
;; this case our handler function. The second argument to `http/start-server`
;; is an option map with the port to listen to.

(comment
  (def server
    (http/start-server root-handler {:port 8081}))

  (.close server)
  )

;; We define a name for our server in order to be able to stop is again.
