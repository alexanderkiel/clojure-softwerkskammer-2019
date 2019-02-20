(ns todo-backend.step-10
  "Step 10 - each new todo has a url, which returns a todo 1/2"
  (:require
    [cheshire.core :as json]
    [clojure.string :as str]
    [todo-backend.util :as util])
  (:import
    [java.util UUID]))

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

(defn render-todo [todo]
  (let [id (:id todo)]
    (assoc todo :url (str "http://localhost:8080/todos/" id))))

;; Now we need to give each todo an ID in order to be able to find it later in
;; the list of todos. We use an UUID here for simplicity. We also convert it
;; into a string in order to be able to compare it with the ID coming from the
;; URL later. We create a `render-todo` function in order to output the full URL.

(defn root-handler [request]
  (case (:request-method request)
    :get
    {:status 200
     :body (mapv render-todo (deref todo-store))}

    :post
    (let [id (str (UUID/randomUUID))
          todo (assoc (:body request)
                 :id id
                 :completed false)]
      (swap! todo-store conj todo)
      {:status 201
       :body (render-todo todo)})

    :delete
    (do
      (reset! todo-store [])
      {:status 204})

    {:status 405}))

;; To answer the GET requests for single todos, we create a `todo-handler`. In
;; that, we cut the ID out of the URL by splitting it and taking the last part.
;; We'll implement proper URL parsing later. Also only for now, we just filter
;; the list of todos by ID. Here `#(= id (:id %))` is a short variant to write
;; an anonymous function were `%` is the only argument (in this case the todo).

(defn todo-handler [request]
  (let [uri (:uri request)
        id (last (str/split uri #"/"))]
    (if-let [todo (first (filter #(= id (:id %)) (deref todo-store)))]
      {:status 200
       :body (render-todo todo)}
      {:status 404})))

;; Because now we have two handlers, we need to implement a routing. To keep it
;; simple we don't use a library for now. A simple `if` will do it here.

(defn router [request]
  (if (= "/" (:uri request))
    (root-handler request)
    (todo-handler request)))

(comment
  (util/restart-server (wrap-json (wrap-cors-headers router)))
  )
