(ns todo-backend.step-11
  "Step 11 - each new todo has a url, which returns a todo 2/2

  We change our token-store to hold a map from todo-id to todo rather than a
  vector of todos."
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

;; We use a map rather than a vector in our store

(def todo-store (atom {}))

(defn render-todo [todo]
  (let [id (:id todo)]
    (assoc todo :url (str "http://localhost:8080/todos/" id))))

;; We use `assoc` instead of `conj` to insert a todo in our store. It's
;; important to remember tha `swap!` will call the function `assoc` with the
;; old value of the store followed by `id` and `todo`.

(defn root-handler [request]
  (case (:request-method request)
    :get
    {:status 200
     :body (mapv render-todo (vals (deref todo-store)))}

    :post
    (let [id (str (UUID/randomUUID))
          todo (assoc (:body request)
                 :id id
                 :completed false)]
      (swap! todo-store assoc id todo)
      {:status 201
       :body (render-todo todo)})

    :delete
    (do
      (reset! todo-store {})
      {:status 204})

    {:status 405}))

;; Having a map in our store, we can access the todo directly by using `get`.

(defn todo-handler [request]
  (let [uri (:uri request)
        id (last (str/split uri #"/"))]
    (if-let [todo (get (deref todo-store) id)]
      {:status 200
       :body (render-todo todo)}
      {:status 404})))

(defn router [request]
  (if (= "/" (:uri request))
    (root-handler request)
    (todo-handler request)))

(comment
  (util/restart-server (wrap-json (wrap-cors-headers router)))
  )
