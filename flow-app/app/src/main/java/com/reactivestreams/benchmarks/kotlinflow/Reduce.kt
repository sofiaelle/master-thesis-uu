package com.reactivestreams.benchmarks.kotlinflow

import android.os.SystemClock
import android.util.Log
import com.reactivestreams.benchmarks.BenchmarkAttributes
import com.reactivestreams.benchmarks.BenchmarkInfo
import com.reactivestreams.benchmarks.SaveBenchmark
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


class Reduce {

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

    private fun setupReduce(
        N: Int,
        info: BenchmarkInfo,
        attributes: BenchmarkAttributes
    ): Flow<Int> =
        flow {
            (0..N)
                .asFlow()
                .onCompletion {
                    val currentTime = SystemClock.elapsedRealtime()
                    if (decrementAndCheckTime(currentTime) == 0) {
                        SaveBenchmark.finishedBenchmark(info, attributes, elapsedTime ?: 0)
                    }
                }
                .reduce { x: Int, y: Int -> x + y }
        }

    private fun performReduce(
        N: Int,
        info: BenchmarkInfo,
        attributes: BenchmarkAttributes
    ) {
        CoroutineScope(Dispatchers.Default).launch {
            setupReduce(N, info, attributes).collect()
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
            performReduce(N, info, attributes)
        }
    }
}