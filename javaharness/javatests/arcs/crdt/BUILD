load("//third_party/java/arcs/build_defs:native.oss.bzl", "java_test")

licenses(["notice"])

java_test(
    name = "AllTests",
    size = "small",
    srcs = glob(["*.java"]),
    test_class = "arcs.crdt.AllTests",
    deps = [
        "//javaharness/java/arcs/api:api-android",
        "//javaharness/java/arcs/crdt:crdt-android",
        "//third_party/java/junit",
    ],
)
