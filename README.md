#tesla

Tesla is a set of components to build microservices in Clojure.
It is built upon [Mount](https://github.com/tolitius/mount) to support stateful components in a
very clojuresque way.


These are the (stateful) components defined by Tesla:

  * a metrics reporter to gather and distribute metrics
  * a handler component to manage your http handlers and register them to your http server.
  * an http server (actually, it's httpkit with a minimal wrapper)
  * a state to aggregate component state to an appstate, so we can get a view into the state of our application as a whole. Register your components to use this.
  * a health check you can set to UNHEALTHY from your code (will be set if Tesla is shutting down). This will give your loadbalancers a signal to take a certain instance off the balancing.
  * a scheduling component based upon Quarzite.
  * a Shutdown mechanism. If necessary delayed, so load-balancers have time to notice.

Tesla is a fork of [Tesla Microservices by Otto.de](https://github.com/otto-de/tesla-microservice), or to be more precise one of [Kai Brandes' fork](https://github.com/kaibra/mount-ms).

[![Build Status](https://travis-ci.org/gorillalabs/tesla.svg)](https://travis-ci.org/gorillalabs/tesla)
[![Dependencies Status](http://jarkeeper.com/gorillalabs/tesla/status.svg)](http://jarkeeper.com/gorillalabs/tesla)
[![Downloads](https://jarkeeper.com/gorillalabs/tesla/downloads.svg)](https://jarkeeper.com/gorillalabs/tesla)

## Breaking changes

_tesla_ is used for a number of different services now. Still it is a work in progress. See [CHANGES.md](./CHANGES.md) for instructions on breaking changes.

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

## Examples (for Ottos tesla-microservice, to be adapted to tesla)

* A growing set of example apllications can be found at [tesla-examples](https://github.com/otto-de/tesla-examples).
* David & Germán created an example application based, among other, on tesla. They wrote a very instructive [blog post about it](http://blog.agilityfeat.com/2015/03/clojure-walking-skeleton/)
* Moritz created [tesla-pubsub-service](https://bitbucket.org/DerGuteMoritz/tesla-pubsub-service). It showcases how to connect components via core.async channels. Also the embedded jetty was replaced by immutant.

## Can I Contribute?

Yes, just fork the Github repo, make changes and create a pull request against the ```develop``` branch. Make sure to have documentation, tests, etc. Whatever it takes to
 be a good OSS citizen.

## License
Apache License
