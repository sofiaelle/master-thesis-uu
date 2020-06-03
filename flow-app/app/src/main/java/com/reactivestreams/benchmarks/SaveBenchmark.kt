package com.reactivestreams.benchmarks

import android.os.Debug
import java.util.concurrent.atomic.AtomicInteger

object SaveBenchmark {

    fun finishedBenchmark(
        info: BenchmarkInfo,
        attributes: BenchmarkAttributes,
        elapsedTime: Long
    ) {
        when (info.metric) {
            BenchmarkMetric.GARBAGE_COLLECTION -> returnGCResult(info, attributes)
            BenchmarkMetric.MEMORY_CONSUMPTION -> returnMCResult(info)
            BenchmarkMetric.RUNTIME -> returnRuntimeResult(info, elapsedTime)
        }
    }

    private fun returnGCResult(
        info: BenchmarkInfo,
        attributes: BenchmarkAttributes
    ) {
        /*val endCount: Int = Debug.getRuntimeStat("art.gc.gc-count").toInt()

        val endTime: Int = Debug.getRuntimeStat("art.gc.gc-time").toInt()
        val endBytesFreed: Long = Debug.getRuntimeStat("art.gc.bytes-freed").toLong()
        val endBytesAlloc: Long = Debug.getRuntimeStat("art.gc.bytes-allocated").toLong()

        val gcAttributes = attributes.getAttributes()

        val count = endCount - gcAttributes[0]
        val time = endTime - gcAttributes[1]
        val bytesFreed = endBytesFreed - gcAttributes[2]
        val bytesAlloc = endBytesAlloc - gcAttributes[3]

        // Force garbage collection event to not leak allocations for next benchmark
        System.gc()*/

        val gcAttributes = attributes.getAttributes()

        saveBenchmark(
            GarbageCollectionResult(
                info,
                gcAttributes[0],
                gcAttributes[1],
                gcAttributes[2],
                gcAttributes[3]
            )
        )
    }

    private fun returnMCResult(
        info: BenchmarkInfo
    ) {
        Debug.stopAllocCounting()

        // Thread local count of objects allocated by the runtime between start and stop counting methods
        val allocCount = Debug.getThreadAllocCount()
        // Thread local size of objects allocated by the runtime between start and stop counting methods
        val allocSize = Debug.getThreadAllocSize()
        val bytesPerObj = allocSize.toDouble() / allocCount

        saveBenchmark(
            MemoryConsumptionResult(
                info,
                allocCount,
                allocSize,
                bytesPerObj
            )
        )
    }

    private fun returnRuntimeResult(
        info: BenchmarkInfo,
        elapsedTime: Long
    ) {
        saveBenchmark(RuntimeResult(info, elapsedTime.toDouble()))
    }

    private fun saveBenchmark(result: BenchmarkResult) {
        Result.benchmarkResult.add(result)
        val count = IterationCounter.benchmarkIterationsPerformed.incrementAndGet()
        if (count == MainActivity.BENCHMARK_ITERATIONS) {
            if (result.getBenchmarkInformation().metric == BenchmarkMetric.GARBAGE_COLLECTION) {
                val endCount: Int = Debug.getRuntimeStat("art.gc.gc-count").toInt()

                val endTime: Int = Debug.getRuntimeStat("art.gc.gc-time").toInt()
                val endBytesFreed: Long = Debug.getRuntimeStat("art.gc.bytes-freed").toLong()
                val endBytesAlloc: Long = Debug.getRuntimeStat("art.gc.bytes-allocated").toLong()

                val gcAttributes = result.getParameters()
                val count = endCount - gcAttributes[0]
                val time = endTime - gcAttributes[1]
                val bytesFreed = endBytesFreed - gcAttributes[2]
                val bytesAlloc = endBytesAlloc - gcAttributes[3]

                Result.benchmarkResult.add(GarbageCollectionResult(
                    result.getBenchmarkInformation(),
                    count,
                    time,
                    bytesFreed,
                    bytesAlloc
                ))
            }
            ResultToFile(Result.benchmarkResult).launchAsyncLog()
        }
    }

    fun resetCounterAndResult() {
        IterationCounter.benchmarkIterationsPerformed.set(0)
        Result.benchmarkResult = mutableListOf()
    }

    object IterationCounter {
        var benchmarkIterationsPerformed: AtomicInteger = AtomicInteger()
    }

    object Result {
        var benchmarkResult: MutableList<BenchmarkResult?> = mutableListOf()
    }
}