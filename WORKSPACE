git_repository(
    name = "org_pubref_rules_kotlin",
    remote = "https://github.com/pubref/rules_kotlin.git",
    commit = "08ca6c7193732463cf45bc41c84776928671edd8",
)

load("@org_pubref_rules_kotlin//kotlin:rules.bzl", "kotlin_repositories")

kotlin_repositories()

maven_jar(
    name = "junit4",
    artifact = "junit:junit:4.12",
)

http_archive(
    name = "pythagoras_kt",
    strip_prefix = "pythagoras.kt-master",
    urls = ["https://github.com/cdietze/pythagoras.kt/archive/master.zip"],
    sha256 = "5474ddfa21222506842659afddbd9b39bc6f7cfb9f63d811a7ea3ebb3a6aaff6"
)
# Uncomment for local development
#local_repository(
#    name = "pythagoras_kt",
#    path = "../pythagoras.kt",
#)

http_archive(
    name = "react_kt",
    strip_prefix = "react.kt-master",
    urls = ["https://github.com/cdietze/react.kt/archive/master.zip"],
    sha256 = "361af71e6d044da65d7a1112c29b23ee06a86aafa46ad6ce9dad96b8443f753e"
)
# Uncomment for local development
#local_repository(
#    name = "react_kt",
#    path = "../react.kt",
#)

# JVM dependencies

maven_jar(
	name = "org_jetbrains_kotlin_kotlin_stdlib_jre7",
	artifact = "org.jetbrains.kotlin:kotlin-stdlib-jre7:1.1.2",
	sha1 = "8fe14858be2d85bb2c8ad1e060f6dafbd45f48f4",
)
maven_jar(
	name = "org_jetbrains_kotlin_kotlin_stdlib_jre8",
	artifact = "org.jetbrains.kotlin:kotlin-stdlib-jre8:1.1.2",
	sha1 = "cd6d8b7a32971564fab0846009593f3bfabdcac1",
)
maven_jar(
	name = "org_lwjgl_lwjgl",
	artifact = "org.lwjgl:lwjgl:3.1.1",
	sha1 = "02b449bd5d8738abb138d6ad89d04b83d4ede504",
)
maven_jar(
	name = "org_lwjgl_lwjgl_glfw",
	artifact = "org.lwjgl:lwjgl-glfw:3.1.1",
	sha1 = "292401460b1030da5626c23b9930fef29421d354",
)
maven_jar(
	name = "org_lwjgl_lwjgl_jemalloc",
	artifact = "org.lwjgl:lwjgl-jemalloc:3.1.1",
	sha1 = "22d6f36387dbe8b13a419a8f8202b615144c7c49",
)
maven_jar(
	name = "org_lwjgl_lwjgl_opengl",
	artifact = "org.lwjgl:lwjgl-opengl:3.1.1",
	sha1 = "bf95209b5d3d484871812b5a7f5d8a9a147bdd0e",
)
maven_jar(
	name = "org_java_websocket_Java_WebSocket",
	artifact = "org.java-websocket:Java-WebSocket:1.3.0",
	sha1 = "db2658f013fb163de4a99274c2eb9e17efe535d2",
)
maven_jar(
	name = "com_googlecode_soundlibs_mp3spi",
	artifact = "com.googlecode.soundlibs:mp3spi:1.9.5.4",
	sha1 = "3bdf9ffd65c157daec8735e127c3705513bd384d",
)
maven_jar(
	name = "com_googlecode_soundlibs_jlayer",
	artifact = "com.googlecode.soundlibs:jlayer:1.0.1.4",
	sha1 = "b8df07ad72b482f7d6e6dee7fed669385e8dd92f",
)
maven_jar(
	name = "com_googlecode_soundlibs_tritonus_share",
	artifact = "com.googlecode.soundlibs:tritonus-share:0.3.7.4",
	sha1 = "bdddc55194f9cf7b970dd5f3affcbacb88342b0b",
)

http_jar(
    name = "org_lwjgl_lwjgl_natives_macos",
    url = "http://repo1.maven.org/maven2/org/lwjgl/lwjgl/3.1.1/lwjgl-3.1.1-natives-macos.jar",
)

http_jar(
    name = "org_lwjgl_lwjgl_glfw_natives_macos",
    url = "http://repo1.maven.org/maven2/org/lwjgl/lwjgl-glfw/3.1.1/lwjgl-glfw-3.1.1-natives-macos.jar",
)

http_jar(
    name = "org_lwjgl_lwjgl_jemalloc_natives_macos",
    url = "http://repo1.maven.org/maven2/org/lwjgl/lwjgl-jemalloc/3.1.1/lwjgl-jemalloc-3.1.1-natives-macos.jar",
)

http_jar(
    name = "org_lwjgl_lwjgl_opengl_natives_macos",
    url = "http://repo1.maven.org/maven2/org/lwjgl/lwjgl-opengl/3.1.1/lwjgl-opengl-3.1.1-natives-macos.jar",
)
