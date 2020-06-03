package com.reactivestreams.benchmarks

interface BenchmarkAttributes {
    fun getAttributes(): ArrayList<Double>
}

class RuntimeAttributes(private val startTime: Long) : BenchmarkAttributes {
    override fun getAttributes(): ArrayList<Double> {
        return arrayListOf(
            startTime.toDouble()
        )
    }
}

class GarbageCollectionAttributes(
    private val startCount: Int,
    private val startTime: Int,
    private val startBytesFreed: Long,
    private val startBytesAlloc: Long
) : BenchmarkAttributes {
    override fun getAttributes(): ArrayList<Double> {
        return arrayListOf(
            startCount.toDouble(),
            startTime.toDouble(),
            startBytesFreed.toDouble(),
            startBytesAlloc.toDouble()
        )
    }
}

class MemoryConsumptionAttributes: BenchmarkAttributes {
    override fun getAttributes(): ArrayList<Double> {
        return arrayListOf()
    }
}