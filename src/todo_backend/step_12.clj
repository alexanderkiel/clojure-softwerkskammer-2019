(ns todo-backend.step-12
  "Step 12 - can change the todo's title by PATCHing to the todo's url"
  (:require
    [cheshire.core :as json]
    [clojure.string :as str]
    [todo-backend.util :as util])
  (:import
    [java.util UUID]))

;; We need to allow PATCH here

(defn wrap-cors-headers [handler]
  (fn [request]
    (if (= :options (:request-method request))
      {:status 200
       :headers
       {"access-control-allow-origin" "*"
        "access-control-allow-headers" "Content-Type"
        "access-control-allow-methods" "DELETE, PATCH"}}
      (assoc (handler request)
        :headers
        {"access-control-allow-origin" "*"
         "access-control-allow-headers" "Content-Type"}))))

(defn parse-json [request]
  (if (#{:post :patch} (:request-method request))
    (update request :body (comp json/parse-string slurp))
    request))

(defn wrap-json [handler]
  (fn [request]
    (let [request (parse-json request)
          response (handler request)]
      (update response :body json/generate-string))))

(def todo-store (atom {}))

(defn render-todo [todo]
  (let [id (:id todo)]
    (assoc todo :url (str "http://localhost:8080/todos/" id))))

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

;; In order to be able to patch todos residing in our todo-store atomically, we
;; need one function which does both: finding the todo and patching it. The
;; `update` function is a perfect match for this task. We used it before
;; updating the body a a request. Here we like to update the todo in a map from
;; ID to todo. With `(update todo-store id f x)` we give `update` a function `f`
;; and a value `x` were `f` will called with the old todo and the value `x`. As
;; function `f` we use `merge` here which merges the kv-pairs from two maps.
;; With that `merge` is essentially a synonym for patch.

(defn patch-todo-at [todo-store id patch]
  (update todo-store id merge patch))

;; You can try `patch-todo-at` here:

(comment
  (patch-todo-at {1 {:id 1 :completed false :title "old"}} 1 {:title "new"})
  )

;; We have to dispatch on request method here, adding the PATCH case. As we get
;; the patch in `(:body request)`, we use our `patch-todo-at` function to
;; patch it atomically in our todo-store using `swap!`. The result of the
;; `swap!` function is the map of our todos after the patch. In order to return
;; the resulting new todo, we use `get` to retrieve it from the map of new
;; todos.

(defn todo-handler [request]
  (let [uri (:uri request)
        id (last (str/split uri #"/"))]
    (if-let [todo (get (deref todo-store) id)]
      (case (:request-method request)
        :get
        {:status 200
         :body (render-todo todo)}

        :patch
        (let [new-todos (swap! todo-store patch-todo-at id (:body request))]
          {:status 200
           :body (render-todo (get new-todos id))})

        {:status 405})
      {:status 404})))

(defn router [request]
  (if (= "/" (:uri request))
    (root-handler request)
    (todo-handler request)))

(comment
  (util/restart-server (wrap-json (wrap-cors-headers router)))
  )
