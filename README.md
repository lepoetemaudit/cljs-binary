# cljs-binary

Early stage experiment in creating a simple DSL for reading packed binary data. Some libraries exist for Clojure, but nothing that does quite what I need for ClojureScript (reading in binary data down to the bit level, understanding endianness etc).

## Overview

Reading packed binary data is as easy as:

```clojure
(read-spec packed-data [:name :charstring-4
			:version :uint8
			:simple-flag :bit
			:another-flag :bit
			:6bit-number :ubitnum-6])
```

## Setup

To get an interactive development environment run:

    lein figwheel

and open your browser at [localhost:3449](http://localhost:3449/).
This will auto compile and send all changes to the browser without the
need to reload. After the compilation process is complete, you will
get a Browser Connected REPL. An easy way to try it is:

    (js/alert "Am I connected?")

and you should see an alert in the browser window.

To clean all compiled files:

    lein clean

To create a production build run:

    lein cljsbuild once min

And open your browser in `resources/public/index.html`. You will not
get live reloading, nor a REPL. 

## License

See `LICENSE`
