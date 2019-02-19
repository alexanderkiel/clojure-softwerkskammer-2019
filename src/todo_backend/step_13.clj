(ns todo-backend.step-13
  "Step 13 - can delete a todo making a DELETE request to the todo's url"
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

(defn patch-todo-at [todo-store id patch]
  (update todo-store id merge patch))

;; We just add a DELETE case here using `dissoc` to remove the todo from our
;; store.

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

        :delete
        (do
          (swap! todo-store dissoc id)
          {:status 204})

        {:status 405})
      {:status 404})))

(defn router [request]
  (if (= "/" (:uri request))
    (root-handler request)
    (todo-handler request)))

(comment
  (util/restart-server (wrap-json (wrap-cors-headers router)))
  )
