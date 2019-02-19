(ns todo-backend.step-2
  "Step 2 - Adding the CORS headers."
  (:require
    [todo-backend.util :as util]))

;; We use the `:headers` key of the response map to return the required CORS
;; headers. The headers itself are also specified by a map. Nothing special here.

(defn root-handler [request]
  {:status 200
   :headers
   {"access-control-allow-origin" "*"
    "access-control-allow-headers" "Content-Type"}
   :body "Hello World"})

(comment
  (util/restart-server root-handler)
  )
