package com.reactivestreams.benchmarks

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Debug
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.IllegalArgumentException

class MainActivity : AppCompatActivity() {

    private var benchmarkHandler: BenchmarkHandler? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                1
            )
        }

        benchmarkHandler = BenchmarkHandler()
        createSpinners()
        addButtonEventListener()
    }

    private fun createSpinners() {
        val spinners = arrayOf(
            Pair(
                this.BenchmarkOperatorSpinner,
                resources.getStringArray(R.array.BenchmarkOperatorSpinner)
            ),
            Pair(
                this.BenchmarkTypeSpinner,
                resources.getStringArray(R.array.BenchmarkTypeSpinner)
            ),
            Pair(
                this.BenchmarkMetricSpinner,
                resources.getStringArray(R.array.BenchmarkMetricSpinner)
            )
        )
        spinners.forEach { (spinner, data) ->
            spinner.adapter = ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, data
            )
        }
    }

    private fun addButtonEventListener() {
        RunBenchmarkButton.setOnClickListener {
            // TODO: Add if statement if already benchmarking
            startBenchmark()
        }
    }

    private fun startBenchmark() {
        val type: BenchmarkType = getBenchmarkType()
        val op: BenchmarkOperator = getBenchmarkOperator()
        val metric: BenchmarkMetric = getBenchmarkMetric()
        repeatAndSaveBenchmark(BenchmarkInfo(type, op, metric))
    }

    private fun repeatAndSaveBenchmark(
        info: BenchmarkInfo
    ) {
        SaveBenchmark.resetCounterAndResult()
        if (info.metric == BenchmarkMetric.GARBAGE_COLLECTION) {
            val startCount = Debug.getRuntimeStat("art.gc.gc-count").toInt()
            val startTime = Debug.getRuntimeStat("art.gc.gc-time").toInt()
            val startBytesAlloc = Debug.getRuntimeStat("art.gc.bytes-allocated").toLong()
            val startBytesFreed = Debug.getRuntimeStat("art.gc.bytes-freed").toLong()
            repeat(BENCHMARK_ITERATIONS) {
                benchmarkHandler?.executeGarbageCollectionBenchmark(
                    info,
                    GarbageCollectionAttributes(
                        startCount,
                        startTime,
                        startBytesFreed,
                        startBytesAlloc
                    )
                )
            }

        } else {
            repeat(BENCHMARK_ITERATIONS) {
                benchmarkHandler?.measureBenchmarkMetric(info, null)
            }
        }

    }


    private fun getBenchmarkMetric(): BenchmarkMetric {
        return when (this.BenchmarkMetricSpinner.selectedItem.toString()) {
            "Garbage collection" -> BenchmarkMetric.GARBAGE_COLLECTION
            "Memory consumption" -> BenchmarkMetric.MEMORY_CONSUMPTION
            "Runtime" -> BenchmarkMetric.RUNTIME
            else -> throw IllegalArgumentException("Invalid spinner value for Benchmark Metric")
        }
    }

    private fun getBenchmarkOperator(): BenchmarkOperator {
        return when (this.BenchmarkOperatorSpinner.selectedItem.toString()) {
            "Map" -> BenchmarkOperator.MAP
            "Filter" -> BenchmarkOperator.FILTER
            "Reduce" -> BenchmarkOperator.REDUCE
            "Take" -> BenchmarkOperator.TAKE
            "Zip" -> BenchmarkOperator.ZIP
            "FlatMap" -> BenchmarkOperator.FLATMAP
            else -> throw IllegalArgumentException("Invalid spinner value for Benchmark Operator")
        }
    }

    private fun getBenchmarkType(): BenchmarkType {
        return when (this.BenchmarkTypeSpinner.selectedItem.toString()) {
            "Kotlin Flow" -> BenchmarkType.KOTLINFLOW
            "RxJava" -> BenchmarkType.RXJAVA
            else -> throw IllegalArgumentException("Invalid spinner value for Benchmark Type")
        }
    }

    companion object {
        const val BENCHMARK_ITERATIONS: Int = 110

    }

}
