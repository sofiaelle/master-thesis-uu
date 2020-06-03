package com.reactivestreams.benchmarks

import android.os.Debug.*
import android.os.SystemClock
import com.reactivestreams.benchmarks.kotlinflow.Filter as FlowFilter
import com.reactivestreams.benchmarks.kotlinflow.FlatMapMerge as FlowFlatMapMerge
import com.reactivestreams.benchmarks.kotlinflow.Map as FlowMap
import com.reactivestreams.benchmarks.kotlinflow.Reduce as FlowReduce
import com.reactivestreams.benchmarks.kotlinflow.Take as FlowTake
import com.reactivestreams.benchmarks.kotlinflow.Zip as FlowZip

import com.reactivestreams.benchmarks.rxjava.FlatMap as RxFlatMap
import com.reactivestreams.benchmarks.rxjava.Filter as RxFilter
import com.reactivestreams.benchmarks.rxjava.Map as RxMap
import com.reactivestreams.benchmarks.rxjava.Reduce as RxReduce
import com.reactivestreams.benchmarks.rxjava.Take as RxTake
import com.reactivestreams.benchmarks.rxjava.Zip as RxZip


class BenchmarkHandler {

    private var runtimeStart: Long = 0


    fun measureBenchmarkMetric(
        info: BenchmarkInfo,
        gcAttributes: BenchmarkAttributes?
    ) {
        return when (info.metric) {
            BenchmarkMetric.GARBAGE_COLLECTION -> {
                if (gcAttributes != null) {
                    executeGarbageCollectionBenchmark(info, gcAttributes)
                } else {
                }
            }
            BenchmarkMetric.MEMORY_CONSUMPTION -> {
                executeMemoryConsumptionBenchmark(info)
            }
            BenchmarkMetric.RUNTIME -> {
                executeRuntimeBenchmark(info)
            }
        }
    }

    private fun executeBenchmark(
        info: BenchmarkInfo,
        attributes: BenchmarkAttributes
    ) {
        when (info.operator) {
            BenchmarkOperator.FILTER -> {
                when (info.type) {
                    BenchmarkType.KOTLINFLOW -> FlowFilter().runBenchmark(
                        BENCHMARK_LIST_LENGTH,
                        BENCHMARK_STREAMS_CREATED,
                        info, attributes
                    )
                    BenchmarkType.RXJAVA -> RxFilter().runBenchmark(
                        BENCHMARK_LIST_LENGTH,
                        BENCHMARK_STREAMS_CREATED,
                        info, attributes
                    )
                }
            }
            BenchmarkOperator.MAP -> {
                when (info.type) {
                    BenchmarkType.KOTLINFLOW -> FlowMap().runBenchmark(
                        BENCHMARK_LIST_LENGTH,
                        BENCHMARK_STREAMS_CREATED,
                        info, attributes
                    )
                    BenchmarkType.RXJAVA -> RxMap().runBenchmark(
                        BENCHMARK_LIST_LENGTH,
                        BENCHMARK_STREAMS_CREATED,
                        info, attributes
                    )
                }
            }
            BenchmarkOperator.REDUCE -> {
                when (info.type) {
                    BenchmarkType.KOTLINFLOW -> FlowReduce().runBenchmark(
                        BENCHMARK_LIST_LENGTH,
                        BENCHMARK_STREAMS_CREATED,
                        info, attributes
                    )
                    BenchmarkType.RXJAVA -> RxReduce().runBenchmark(
                        BENCHMARK_LIST_LENGTH,
                        BENCHMARK_STREAMS_CREATED,
                        info, attributes
                    )
                }
            }
            BenchmarkOperator.TAKE -> {
                when (info.type) {
                    BenchmarkType.KOTLINFLOW -> FlowTake().runBenchmark(
                        BENCHMARK_LIST_LENGTH,
                        BENCHMARK_STREAMS_CREATED,
                        info, attributes
                    )
                    BenchmarkType.RXJAVA -> RxTake().runBenchmark(
                        BENCHMARK_LIST_LENGTH,
                        BENCHMARK_STREAMS_CREATED,
                        info, attributes
                    )
                }
            }
            BenchmarkOperator.ZIP -> {
                when (info.type) {
                    BenchmarkType.KOTLINFLOW -> FlowZip().runBenchmark(
                        BENCHMARK_LIST_LENGTH,
                        BENCHMARK_STREAMS_CREATED,
                        info, attributes
                    )
                    BenchmarkType.RXJAVA -> RxZip().runBenchmark(
                        BENCHMARK_LIST_LENGTH,
                        BENCHMARK_STREAMS_CREATED,
                        info, attributes
                    )
                }
            }
            BenchmarkOperator.FLATMAP -> {
                when (info.type) {
                    BenchmarkType.KOTLINFLOW -> FlowFlatMapMerge().runBenchmark(
                        BENCHMARK_LIST_LENGTH,
                        BENCHMARK_STREAMS_CREATED,
                        info, attributes
                    )
                    BenchmarkType.RXJAVA -> RxFlatMap().runBenchmark(
                        BENCHMARK_LIST_LENGTH,
                        BENCHMARK_STREAMS_CREATED,
                        info, attributes
                    )
                }
            }
        }
    }

    fun executeGarbageCollectionBenchmark(
        info: BenchmarkInfo,
        gcAttributes: BenchmarkAttributes
    ) {
        executeBenchmark(
            info,
            gcAttributes
        )
    }

    private fun executeMemoryConsumptionBenchmark(
        info: BenchmarkInfo
    ) {
        // Deprecated methods because: 'Accurate counting is a burden on the runtime and may be removed.'
        // However, since runtime is not measured in this benchmark, this may be disregarded
        resetGlobalAllocCount()
        resetGlobalAllocSize()
        startAllocCounting()

        executeBenchmark(info, MemoryConsumptionAttributes())
    }


    private fun executeRuntimeBenchmark(
        info: BenchmarkInfo
    ) {
        runtimeStart = SystemClock.elapsedRealtimeNanos()
        executeBenchmark(info, RuntimeAttributes(runtimeStart))
    }


    companion object {
        const val BENCHMARK_LIST_LENGTH: Int = 8
        const val BENCHMARK_STREAMS_CREATED = 1000
    }
}