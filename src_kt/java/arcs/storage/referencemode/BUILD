load(
    "//third_party/java/arcs/build_defs:build_defs.bzl",
    "kt_jvm_and_js_library",
)

licenses(["notice"])

package(default_visibility = ["//visibility:public"])

kt_jvm_and_js_library(
    name = "referencemode",
    srcs = glob(["*.kt"]),
    deps = [
        "//src_kt/java/arcs/common",
        "//src_kt/java/arcs/crdt",
        "//src_kt/java/arcs/crdt/internal",
        "//src_kt/java/arcs/data:entity",
        "//src_kt/java/arcs/storage:proxy",
        "//src_kt/java/arcs/storage:storage_key",
        "//src_kt/java/arcs/util",
        "//third_party/kotlin/kotlinx/kotlinx_atomicfu",
        "//third_party/kotlin/kotlinx/kotlinx_coroutines",
    ],
)
