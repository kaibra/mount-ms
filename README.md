#tesla

Tesla is a basic set of components to build microservices in Clojure plus some useful components.
It is built upon [Mount](https://github.com/tolitius/mount) to support stateful components in a
very clojuresque way.


These are the (stateful) components defined by Tesla (```gorillalabs.tesla/core```) itself:

  * a metrics reporter to gather and distribute metrics
  * a handler component to manage your http handlers and register them to your http server.
  * an http server (actually, it's httpkit with a minimal wrapper)
  * a state to aggregate component state to an appstate, so we can get a view into the state of our application as a whole. Register your components to use this.
  * a health check you can set to UNHEALTHY from your code (will be set if Tesla is shutting down). This will give your loadbalancers a signal to take a certain instance off the balancing.
  * a scheduling component based upon Quarzite.
  * a Shutdown mechanism. If necessary delayed, so load-balancers have time to notice.

Tesla is a fork of [Tesla Microservices by Otto.de](https://github.com/otto-de/tesla-microservice), or to be more precise one of [Kai Brandes' fork](https://github.com/kaibra/mount-ms).

[![Build Status](https://travis-ci.org/gorillalabs/tesla.svg)](https://travis-ci.org/gorillalabs/tesla)

## gorillalabs/tesla (Parent project)
[![Dependencies Status](http://jarkeeper.com/gorillalabs/tesla/status.svg)](http://jarkeeper.com/gorillalabs.tesla/core)
[![Downloads](https://jarkeeper.com/gorillalabs/tesla/downloads.svg)](https://jarkeeper.com/gorillalabs.tesla/core)
[![Clojars Project](https://img.shields.io/clojars/v/gorillalabs.tesla/core.svg)](https://clojars.org/gorillalabs.tesla/core)

## gorillalabs.tesla/core
[![Downloads](https://jarkeeper.com/gorillalabs.tesla/core/downloads.svg)](https://jarkeeper.com/gorillalabs.tesla/core)
[![Clojars Project](https://img.shields.io/clojars/v/gorillalabs.tesla/core.svg)](https://clojars.org/gorillalabs.tesla/core)

## gorillalabs.tesla/titan
[![Downloads](https://jarkeeper.com/gorillalabs.tesla/titan/downloads.svg)](https://jarkeeper.com/gorillalabs.tesla/titan)
[![Clojars Project](https://img.shields.io/clojars/v/gorillalabs.tesla/titan.svg)](https://clojars.org/gorillalabs.tesla/titan)

## gorillalabs.tesla/mongo
[![Downloads](https://jarkeeper.com/gorillalabs.tesla/mongo/downloads.svg)](https://jarkeeper.com/gorillalabs.tesla/mongog)
[![Clojars Project](https://img.shields.io/clojars/v/gorillalabs.tesla/mongo.svg)](https://clojars.org/gorillalabs.tesla/mongo)

## gorillalabs.tesla/quarzite
[![Downloads](https://jarkeeper.com/gorillalabs.tesla/quartzite/downloads.svg)](https://jarkeeper.com/gorillalabs.tesla/quartzite)
[![Clojars Project](https://img.shields.io/clojars/v/gorillalabs.tesla/quartzite.svg)](https://clojars.org/gorillalabs.tesla/quartzite)

## gorillalabs.tesla/sente
[![Downloads](https://jarkeeper.com/gorillalabs.tesla/sente/downloads.svg)](https://jarkeeper.com/gorillalabs.tesla/sente)
[![Clojars Project](https://img.shields.io/clojars/v/gorillalabs.tesla/sente.svg)](https://clojars.org/gorillalabs.tesla/sente)


## Breaking changes

We us _tesla_ for a number of different services now.
However, it's not a 1.0.0 version and it still is to be considered work in progress.
See [CHANGES.md](./CHANGES.md) for instructions on breaking changes.

```clj
    (ns gorillalabs.ms-example
      (:require [gorillalabs.tesla :as tesla]
                [clojure.tools.logging :as log]
                [gorillalabs.tesla.component.httpkit :as server])
  (:gen-class))

(defn -main [& args]
  (log/info "Starting MS-EXAMPLE")
  (tesla/start
    {:http-server #'server/server}
    ;put your custom states you want to start with the default states here
    ))
```

## Can I Contribute?

Yes, just fork the Github repo, make changes and create a pull request
against the ```develop``` branch.
Make sure to have documentation, tests, etc. Whatever it takes to
be a good OSS citizen.

## License
Apache License
