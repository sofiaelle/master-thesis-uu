package com.reactivestreams.benchmarks

data class BenchmarkInfo(
    val type: BenchmarkType,
    val operator: BenchmarkOperator,
    val metric: BenchmarkMetric
) {

}