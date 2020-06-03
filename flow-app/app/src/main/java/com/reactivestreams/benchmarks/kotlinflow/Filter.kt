package com.reactivestreams.benchmarks.kotlinflow

import android.os.SystemClock
import com.reactivestreams.benchmarks.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class Filter {

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

    private fun setupFlow(
        N: Int,
        info: BenchmarkInfo,
        attributes: BenchmarkAttributes
    ): Flow<Int> {
        return (0..N).asFlow()
            .filter { it * it <= N }
            .flowOn(Dispatchers.Default)
            .onCompletion {
                val currentTime = SystemClock.elapsedRealtime()
                if (decrementAndCheckTime(currentTime) == 0) {
                    SaveBenchmark.finishedBenchmark(info, attributes, elapsedTime?: 0)
                }
            }
    }

    private fun performFilter(
        N: Int,
        info: BenchmarkInfo,
        attributes: BenchmarkAttributes
    ) {
        CoroutineScope(Dispatchers.Default).launch {
            setupFlow(N, info, attributes).collect()
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
            performFilter(N, info, attributes)
        }
    }
}