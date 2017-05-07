Klay
===

A Kotlin game development framework for multiple platforms

This project is still in early development as I am working on the graphics system for the JVM and JS backends.

Building
---

The library is built using [Maven].

Invoke `mvn install` to build and install the library to your local Maven repository (i.e.
`~/.m2/repository`).

Demo
---

To launch the JVM demo app invoke `mvn -Pjvm test`.

To launch the JS demo, build using `mvn install` then open `./tests/js/src/main/resources/index.html` in a browser.


License
---
Klay is released under the Apache License, Version 2.0 which can be found
in the `LICENSE` file and at http://www.apache.org/licenses/LICENSE-2.0 on the
web.
