package com.reactivestreams.benchmarks.rxjava

import android.os.SystemClock
import android.util.Log
import com.reactivestreams.benchmarks.BenchmarkAttributes
import com.reactivestreams.benchmarks.BenchmarkInfo
import com.reactivestreams.benchmarks.SaveBenchmark
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

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

    private fun getReducedValue(
        info: BenchmarkInfo,
        N: Int,
        attributes: BenchmarkAttributes
    ) {
        Observable
            .range(0, N + 1)
            .doOnComplete {
                val currentTime = SystemClock.elapsedRealtime()
                if (decrementAndCheckTime(currentTime) == 0) {
                    SaveBenchmark.finishedBenchmark(info, attributes, elapsedTime ?: 0)
                }
            }
            .reduce { x: Int, y: Int -> x + y }
            .subscribeOn(Schedulers.computation())
            .subscribe()
    }

    fun runBenchmark(
        N: Int,
        streamsCreated: Int,
        info: BenchmarkInfo,
        attributes: BenchmarkAttributes
    ) {
        counter = streamsCreated
        for (i in 0 until streamsCreated) {
            getReducedValue(info, N, attributes)
        }
    }
}