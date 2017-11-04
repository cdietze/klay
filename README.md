Klay
===

[![Build Status](https://travis-ci.org/cdietze/klay.svg?branch=master)](https://travis-ci.org/cdietze/klay)

A Kotlin game development framework for multiple platforms.

Klay is a port in Kotlin of the [PlayN Project](https://github.com/playn/playn).

Currently there is only a JVM backend.

Building
---

The library is built using [Gradle](https://gradle.org/).

Invoke `./gradlew build` to build.

Invoke `./gradlew publishToMavenLocal` to install the JARs to your local maven repository.

Demo
---

`./gradlew :klay-demo:klay-demo-jvm:run` will launch the JVM klay demo.

`./gradlew :tripleklay:tripleklay-demo:tripleklay-demo-jvm:run` will launch the JVM tripleklay demo.
`tripleklay` is a port in Kotlin of the utility library [Triple Play](https://github.com/threerings/tripleplay).

License
---
Klay is released under the Apache License, Version 2.0 which can be found
in the `LICENSE` file and at http://www.apache.org/licenses/LICENSE-2.0 on the
web.
