package com.reactivestreams.benchmarks.rxjava

import android.os.SystemClock
import com.reactivestreams.benchmarks.BenchmarkAttributes
import com.reactivestreams.benchmarks.BenchmarkInfo
import com.reactivestreams.benchmarks.SaveBenchmark
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

class FlatMap {

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

    private fun performFlatMapping(
        info: BenchmarkInfo,
        N: Int,
        attributes: BenchmarkAttributes
    ) {
        Observable
            .range(0, N)
            .flatMap {
                Observable.just("a", "b", "c")
            }.subscribeOn(Schedulers.computation())
            .doOnComplete {
                val currentTime = SystemClock.elapsedRealtime()
                if (decrementAndCheckTime(currentTime) == 0) {
                    SaveBenchmark.finishedBenchmark(info, attributes, elapsedTime ?: 0)
                }
            }
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
            performFlatMapping(info, N, attributes)
        }
    }
}