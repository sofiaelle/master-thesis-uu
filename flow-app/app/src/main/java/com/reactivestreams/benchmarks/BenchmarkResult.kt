package com.reactivestreams.benchmarks

interface BenchmarkResult {
    fun resultToString(): String
    fun getParameters(): ArrayList<Double>
    fun getBenchmarkInformation(): BenchmarkInfo
}

class GarbageCollectionResult(
    private val info: BenchmarkInfo,
    private val count: Double,
    private val time: Double,
    private val bytesFreed: Double,
    private val bytesAlloc: Double
) : BenchmarkResult {
    override fun resultToString(): String {
        return "GC count: $count, GC time taken: ${time / 1000000}, GC bytes freed: $bytesFreed. GC bytes allocated: $bytesAlloc"
    }

    override fun getParameters(): ArrayList<Double> {
        return arrayListOf(
            count,
            time,
            bytesFreed,
            bytesAlloc
        )
    }

    override fun getBenchmarkInformation(): BenchmarkInfo {
        return info
    }
}

class MemoryConsumptionResult(
    private val info: BenchmarkInfo,
    private val allocCount: Int,
    private val allocSize: Int,
    private val bytesPerObj: Double
) : BenchmarkResult {
    override fun resultToString(): String {
        return "Memory objects allocated: $allocCount, memory size in bytes: $allocSize, bytes per object: $bytesPerObj"
    }

    override fun getParameters(): ArrayList<Double> {
        return arrayListOf(
            allocCount.toDouble(),
            allocSize.toDouble(),
            bytesPerObj
        )
    }

    override fun getBenchmarkInformation(): BenchmarkInfo {
        return info
    }
}

class RuntimeResult(
    private val info: BenchmarkInfo,
    private val time: Double
) : BenchmarkResult {

    override fun resultToString(): String {
        return "Runtime: ${time / 1000000} ns"
    }

    override fun getParameters(): ArrayList<Double> {
        return arrayListOf(
            time.toDouble()
        )
    }

    override fun getBenchmarkInformation(): BenchmarkInfo {
        return info
    }
}