Klay
===

[![Build Status](https://travis-ci.org/cdietze/klay.svg?branch=master)](https://travis-ci.org/cdietze/klay)

A Kotlin game development framework for multiple platforms

Klay is a port in Kotlin of the [PlayN Project](https://github.com/playn/playn).
The project is still in early development, the next milestone is to have most subsystems
working for the JVM and JS backends.


Building
---

The library is built using [Maven].

Invoke `mvn install` to build and install the library to your local Maven repository (i.e.
`~/.m2/repository`).


Demo
---

To launch the JVM demo, invoke `mvn -Pjvm test` this will first start the klay-demo and afterwards the tripleklay-demo.

License
---
Klay is released under the Apache License, Version 2.0 which can be found
in the `LICENSE` file and at http://www.apache.org/licenses/LICENSE-2.0 on the
web.


[Maven]: http://maven.apache.org/
