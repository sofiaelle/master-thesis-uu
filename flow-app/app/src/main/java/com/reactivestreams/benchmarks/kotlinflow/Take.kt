package com.reactivestreams.benchmarks.kotlinflow

import android.os.SystemClock
import com.reactivestreams.benchmarks.BenchmarkAttributes
import com.reactivestreams.benchmarks.BenchmarkInfo
import com.reactivestreams.benchmarks.SaveBenchmark
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class Take {

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

    private fun setupTakeFlow(
        N: Int,
        info: BenchmarkInfo,
        attributes: BenchmarkAttributes
    ): Flow<Int> {
        return (0..(N + N))
            .asFlow()
            .take(3)
            .flowOn(Dispatchers.Default)
            .onCompletion {
                val currentTime = SystemClock.elapsedRealtime()
                if (decrementAndCheckTime(currentTime) == 0) {
                    SaveBenchmark.finishedBenchmark(info, attributes, elapsedTime ?: 0)
                }
            }
    }

    private fun collectTakeFlow(
        N: Int,
        info: BenchmarkInfo,
        attributes: BenchmarkAttributes
    ) {
        CoroutineScope(Dispatchers.Default).launch {
            setupTakeFlow(N, info, attributes).collect()
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
            collectTakeFlow(N, info, attributes)
        }
    }
}