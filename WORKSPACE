git_repository(
    name = "org_pubref_rules_kotlin",
    remote = "https://github.com/pubref/rules_kotlin.git",
    tag = "v0.3.1", # update as needed
)

load("@org_pubref_rules_kotlin//kotlin:rules.bzl", "kotlin_repositories")

kotlin_repositories()

maven_jar(
    name = "junit4",
    artifact = "junit:junit:jar:4.12",
)

http_archive(
    name = "pythagoras_kt",
    strip_prefix = "pythagoras.kt-master",
    urls = ["https://github.com/cdietze/pythagoras.kt/archive/master.zip"]
)

http_archive(
    name = "react_kt",
    strip_prefix = "react.kt-master",
    urls = ["https://github.com/cdietze/react.kt/archive/master.zip"]
)
