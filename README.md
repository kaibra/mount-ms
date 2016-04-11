#tesla

This is the common basis to build microservices in Clojure. It is built upon [Mount](https://github.com/tolitius/mount) to support stateful components in a
very clojuresque way.

Tesla is coming with some helpful components, like a health-check, a monitoring component, http handling and serving. You might alter your configuration to add
 your own components and even replace ours.


Tesla is a fork of [Tesla Microservices by Otto.de](https://github.com/otto-de/tesla-microservice), or to be more precise one of [Kai Brandes' fork](https://github.com/kaibra/mount-ms).

[![Build Status](https://travis-ci.org/gorillalabs/tesla.svg)](https://travis-ci.org/gorillalabs/tesla)
[![Dependencies Status](http://jarkeeper.com/gorillalabs/tesla/status.svg)](http://jarkeeper.com/gorillalabs/tesla)
[![Downloads](https://jarkeeper.com/gorillalabs/tesla/downloads.svg)](https://jarkeeper.com/gorillalabs/tesla)

## Breaking changes

_tesla_ is used for a number of different services now. Still it is a work in progress. See [CHANGES.md](./CHANGES.md) for instructions on breaking changes.

-```clj
-(ns kaibra.ms-example
-  (:require
-    [kaibra.system :as mount-ms]
-    [clojure.tools.logging :as log]
-    [kaibra.stateful.server :as server])
-  (:gen-class))
+_tesla_ is used for a number of different services now. Still it is a work in progress. See [CHANGES.md](./CHANGES.md) for instructions on breaking changes.
 
-(defn -main [& args]
-  (log/info "Starting MS-EXAMPLE")
-  (mount-ms/start-with-states
-    #'server/server ;see ms-httpkit
-    ;put your custom states you want to start with the mount-ms states here
-    ))


## Features included

* Load configuration from filesystem.
* Aggregate a status.
* Reply to a health check.
* Deliver a json status report.
* Report to graphite using the metrics library.
* Manage handlers using ring.
* Shutdown gracefully. If necessary delayed, so load-balancers have time to notice.

## Examples (for Ottos tesla-microservice, to be adapted to tesla)

* A growing set of example apllications can be found at [tesla-examples](https://github.com/otto-de/tesla-examples).
* David & Germán created an example application based, among other, on tesla. They wrote a very instructive [blog post about it](http://blog.agilityfeat.com/2015/03/clojure-walking-skeleton/)
* Moritz created [tesla-pubsub-service](https://bitbucket.org/DerGuteMoritz/tesla-pubsub-service). It showcases how to connect components via core.async channels. Also the embedded jetty was replaced by immutant.

## Can I Contribute?

Yes, just fork the Github repo, make changes and create a pull request against the ```develop``` branch. Make sure to have documentation, tests, etc. Whatever it takes to
 be a good OSS citizen.

## License
Apache License
