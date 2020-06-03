package com.reactivestreams.benchmarks.kotlinflow

import android.os.SystemClock
import com.reactivestreams.benchmarks.BenchmarkAttributes
import com.reactivestreams.benchmarks.BenchmarkInfo
import com.reactivestreams.benchmarks.SaveBenchmark
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class Zip {

    private var elapsedTime: Long? = null
    private var counter: Int = 0

    @Synchronized
    private fun decrementAndCheckTime(time: Long): Int {
        if (time > elapsedTime ?: 0) {
            elapsedTime = time
        }
        counter--
        return counter
    }

    private fun setupFlowsAndZip(
        N: Int,
        info: BenchmarkInfo,
        attributes: BenchmarkAttributes
    ): Flow<Int> {
        val f1 = (0..N).asFlow()
        val f2 = (0..N).asFlow()
        return f1.zip(f2) { a, b -> a + b }
            .flowOn(Dispatchers.Default)
            .onCompletion {
                val currentTime = SystemClock.elapsedRealtime()
                if (decrementAndCheckTime(currentTime) == 0) {
                    SaveBenchmark.finishedBenchmark(info, attributes, elapsedTime ?: 0)
                }
            }

    }

    private fun performZip(
        N: Int,
        info: BenchmarkInfo,
        attributes: BenchmarkAttributes
    ) {
        CoroutineScope(Dispatchers.Default).launch {
            setupFlowsAndZip(N, info, attributes).collect()
        }
    }


    fun runBenchmark(
        N: Int,
        streamsCreated: Int,
        info: BenchmarkInfo,
        attributes: BenchmarkAttributes
    ) {
        counter = streamsCreated
        for (i in 0 until streamsCreated) {
            performZip(N, info, attributes)
        }
    }


}