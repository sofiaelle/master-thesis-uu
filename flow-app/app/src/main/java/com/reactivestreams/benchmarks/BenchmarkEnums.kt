package com.reactivestreams.benchmarks

enum class BenchmarkOperator {
    MAP, FILTER, REDUCE, TAKE, ZIP, FLATMAP
}

enum class BenchmarkType {
    RXJAVA, KOTLINFLOW
}

enum class BenchmarkMetric {
    GARBAGE_COLLECTION, MEMORY_CONSUMPTION, RUNTIME
}
