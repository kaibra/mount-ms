#mount-ms

[![Clojars Project](http://clojars.org/kaibra/mount-ms/latest-version.svg)](http://clojars.org/kaibra/mount-ms)   

[![Build Status](https://travis-ci.org/kaibra/mount-ms.svg)](https://travis-ci.org/kaibra/mount-ms)  

This project is derived from tesla-microservice.  
Main differences to the tesla-microservice: mount-ms uses MOUNT instead of component (surprise) :)   
Current available components are: 

    * [ms-httpkit](https://github.com/kaibra/ms-httpkit)

# Usage

```clj
(ns kaibra.ms-example
  (:require
    [kaibra.system :as mount-ms]
    [clojure.tools.logging :as log]
    [kaibra.stateful.server :as server])
  (:gen-class))

(defn -main [& args]
  (log/info "Starting MS-EXAMPLE")
  (mount-ms/start-with-states
    #'server/server ;see ms-httpkit
    ;put your custom states you want to start with the mount-ms states here
    ))

```

# Example
To see a working example of this microservice which includes a server look at [ms-example](https://github.com/kaibra/ms-example)

kaibra
