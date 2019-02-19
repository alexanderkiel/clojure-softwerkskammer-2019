# Kata Driven Todo-Backend Implementation for the 2019 Softwerkskammer Clojure Meetup in Leipzig

## Usage

* install [Leiningen][1]
* check our the repo
* run `lein repl`
* inside the REPL evaluate:
  * `(in-ns 'todo-backend.step-1)`
  * `(def server (http/start-server root-handler {:port 8080}))`
* goto `http://www.todobackend.com/specs/index.html?http://localhost:8080`
* see that nothing works right now
* inside the REPL evaluate:
  * `(.close server)`
  * `(in-ns 'todo-backend.step-2)`
  * `(util/restart-server root-handler)`
* see that the first test is passed

## License

Copyright © 2019 Alexander Kiel

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

[1]: <https://leiningen.org>
