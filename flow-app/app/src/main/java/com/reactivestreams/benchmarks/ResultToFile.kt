package com.reactivestreams.benchmarks

import android.os.Environment
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.io.IOException
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sqrt

class ResultToFile constructor(
    private val result: MutableList<BenchmarkResult?>
) {

    fun launchAsyncLog() {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                logResultsToFile()
            }
        }
    }

    private fun logResultsToFile() {
        var file: File? = null
        var writer: FileWriter? = null
        val info: BenchmarkInfo = result[0]!!.getBenchmarkInformation()
        val fileName = "${info.operator}_${info.type}_${info.metric}"

        try {
            if (isExternalStorageWritable()) {
                val baseDir = Environment.getExternalStorageDirectory().absolutePath
                file = File(baseDir, fileName)
                writer = FileWriter(file)

                if (file.exists() && !file.isDirectory) {
                    writeResults(file.absolutePath, writer, info)
                }
            } else {
                println("External storage not writable")
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("err", "Did not write to file")
        } finally {
            if (writer != null) {
                try {
                    writer.flush()
                    writer.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                    Log.e("err", "Did not close writer")
                }
            }
        }
    }

    private fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    private fun writeResults(
        path: String,
        writer: FileWriter,
        info: BenchmarkInfo
    ) {
        var warmUpResults = arrayListOf<BenchmarkResult>()
        var resultWithoutWarmUp = arrayListOf<BenchmarkResult>()
        var results = result
        if (info.metric == BenchmarkMetric.GARBAGE_COLLECTION) {
            val result = results.last()
            val params = result?.getParameters()
            writer.apply {
                append("\n----------------------------- \n")
                append("Calculated benchmark results: \n")
                append("Total amount of gc count, time, bytes freed and bytes allocated during the benchmark process: \n")
                append("Count: ${params?.get(0)}, time: ${params?.get(1)} ms, freed: ${params?.get(2)}, alloc: ${params?.get(3)} \n")
            }

        } else {
            // Remove first 10 warm up results
            if (result.size > WARM_UP_RESULT_SIZE) {
                // Set first 10 to warm up result
                warmUpResults = results.take(WARM_UP_RESULT_SIZE) as ArrayList<BenchmarkResult>
                // Drop the first 10 from the result list to remove warm up iterations
                resultWithoutWarmUp = result.drop(WARM_UP_RESULT_SIZE) as ArrayList<BenchmarkResult>
            }

            var benchmarkFormat = when (info.metric) {
                BenchmarkMetric.MEMORY_CONSUMPTION -> "[Memory objects allocated, Memory size in bytes, Bytes per object]"
                BenchmarkMetric.GARBAGE_COLLECTION -> "[GC count, GC time (ns), GC bytes freed, GC bytes allocated]"
                BenchmarkMetric.RUNTIME -> "[Runtime (ns)]"
            }

            writer.apply {
                // Add info about the benchmark type, operator and  metric used
                append("\nBenchmark results for ${info.type} using the ${info.operator} operator\n")
                append("Metric: ${info.metric}. Data format: $benchmarkFormat\n")
                append("\n ----------------------------- \n")
                append("Discarded warm up results: \n")
                warmUpResults.forEach { result ->
                    append("${result.resultToString()}\n")
                }
                append("\n\n ----------------------------- \n")
                append("Results: \n $benchmarkFormat\n")
            }
            appendResults(resultWithoutWarmUp, writer, info.metric)
        }
    }

    private fun appendResults(
        results: MutableList<BenchmarkResult>,
        writer: FileWriter,
        metric: BenchmarkMetric
    ) {
        when (metric) {
            BenchmarkMetric.GARBAGE_COLLECTION -> appendGCResult(results, writer)
            BenchmarkMetric.RUNTIME -> appendRuntimeResult(results, writer)
            BenchmarkMetric.MEMORY_CONSUMPTION -> appendMemoryResults(results, writer)
        }
    }

    private fun appendGCResult(results: MutableList<BenchmarkResult>, writer: FileWriter) {
        var resultCount = arrayListOf<Double>()
        var resultTime = arrayListOf<Double>()
        var resultBytesFreed = arrayListOf<Double>()
        var resultBytesAlloc = arrayListOf<Double>()

        results.forEach { result ->
            val resultParameters = result.getParameters()
            resultCount.add(resultParameters[0])
            resultTime.add(resultParameters[1])
            resultBytesFreed.add(resultParameters[2])
            resultBytesAlloc.add(resultParameters[3])
            writer.apply {
                append("${result.resultToString()}\n")
            }
        }

        val aMeanCount = arithmeticMean(resultCount)
        val aMeanTime = arithmeticMean(resultTime)

        val hMeanFreed = harmonicMean(resultBytesFreed)
        val hMeanAlloc = harmonicMean(resultBytesAlloc)

        val sdCount = standardDeviation(resultCount, aMeanCount)
        val sdTime = standardDeviation(resultTime, aMeanTime)
        val sdFreed = standardDeviation(resultBytesFreed, hMeanFreed)
        val sdAlloc = standardDeviation(resultBytesAlloc, hMeanFreed)

        val ciCount = confidenceInterval(aMeanCount, resultCount.size, sdCount)
        val ciTime = confidenceInterval(aMeanTime, resultTime.size, sdTime)
        val ciFreed = confidenceInterval(hMeanFreed, resultBytesFreed.size, sdFreed)
        val ciAlloc = confidenceInterval(hMeanAlloc, resultBytesAlloc.size, sdAlloc)

        writer.apply {
            append("\n----------------------------- \n")
            append("Calculated benchmark results: \n")
            append("Mean (arithmetic and harmonic): \n")
            append("Count: $aMeanCount, time: ${nanoToMilli(aMeanTime)} ms, freed: $hMeanFreed, alloc: $hMeanFreed \n")
            append("Standard deviation: \n")
            append("$sdCount, $sdTime, $sdFreed, $sdAlloc\n")
            append("Confidence interval: \n")
            append("$ciCount, $ciTime, $ciFreed, $ciAlloc\n")
        }
    }

    private fun appendRuntimeResult(results: MutableList<BenchmarkResult>, writer: FileWriter) {
        var resultTime = arrayListOf<Double>()

        results.forEach { result ->
            val resultParameters = result.getParameters()
            resultTime.add(resultParameters[0])
            writer.apply {
                append("${result.resultToString()}\n")
            }
        }

        val aMeanTime = arithmeticMean(resultTime)
        val sdTime = standardDeviation(resultTime, aMeanTime)
        val ciTime = confidenceInterval(aMeanTime, resultTime.size, sdTime)

        writer.apply {
            append("\n----------------------------- \n")
            append("Calculated benchmark results: \n")
            append("Arithmetic mean: \n")
            append("${nanoToMilli(aMeanTime)} ms\n")
            append("Plus minus (confidence interval): \n")
            append("${nanoToMilli(aMeanTime-ciTime.first)}\n")
        }
    }

    private fun appendMemoryResults(results: MutableList<BenchmarkResult>, writer: FileWriter) {
        var resultCount = arrayListOf<Double>()
        var resultSize = arrayListOf<Double>()
        var resultBytesPerObj = arrayListOf<Double>()

        results.forEach { result ->
            val resultParameters = result.getParameters()
            resultCount.add(resultParameters[0])
            resultSize.add(resultParameters[1])
            resultBytesPerObj.add(resultParameters[2])
            writer.apply {
                append("${result.resultToString()}\n")
            }
        }

        val aMeanCount = arithmeticMean(resultCount)
        val aMeanSize = arithmeticMean(resultSize)

        val hMeanBytesPerObj = harmonicMean(resultBytesPerObj)

        val sdCount = standardDeviation(resultCount, aMeanCount)
        val sdSize = standardDeviation(resultSize, aMeanSize)
        val sdBytesPerObj = standardDeviation(resultBytesPerObj, hMeanBytesPerObj)

        val ciCount = confidenceInterval(aMeanCount, resultCount.size, sdCount)
        val ciSize = confidenceInterval(aMeanSize, resultSize.size, sdSize)
        val ciBytesPerObj =
            confidenceInterval(hMeanBytesPerObj, resultBytesPerObj.size, sdBytesPerObj)

        writer.apply {
            append("\n----------------------------- \n")
            append("Calculated benchmark results: \n")
            append("Mean (arithmetic and harmonic): \n")
            append("Count: $aMeanCount, size: $aMeanSize, bytes per object: $hMeanBytesPerObj\n")
            append("Plus minus (confidence interval): \n")
            append("Count: ${aMeanCount-ciCount.first}, size: ${aMeanSize-ciSize.first}, bytes per object: ${hMeanBytesPerObj-ciBytesPerObj.first}\n")
        }
    }


    private fun arithmeticMean(resultArray: ArrayList<Double>): Double {
        return if (resultArray.isNotEmpty()) {
            val size = resultArray.size.toDouble()
            val sum = resultArray.fold(0.0) { sum, nextResult -> sum + nextResult }
            round((sum / size) * 100) / 100.0
        } else 0.0
    }

    private fun harmonicMean(resultArray: ArrayList<Double>): Double {
        return if (resultArray.isNotEmpty()) {
            val size = resultArray.size.toDouble()
            val sum = resultArray.fold(0.0) { sum, nextResult -> sum + 1.0 / nextResult }
            round((size / sum) * 100) / 100.0
        } else 0.0

    }

    private fun standardDeviation(resultArray: ArrayList<Double>, resultMean: Double): Double {
        return if (resultArray.isNotEmpty()) {
            val size = resultArray.size.toDouble()
            val standardDeviation = resultArray.fold(0.0) { sum, nextResult ->
                sum + (nextResult - resultMean).pow(
                    2.0
                )
            }
            round(sqrt(standardDeviation / (size - 1)) * 100 / 100.0)
        } else 0.0
    }

    private fun confidenceInterval(
        resultMean: Double,
        size: Int,
        standardDeviation: Double
    ): Pair<Double, Double> {
        return if (size != 0) {
            val interval = 1.96 * (standardDeviation / sqrt(size.toDouble()))
            val confAddition = resultMean + interval
            val confSubtraction = resultMean - interval
            Pair(
                round(confSubtraction * 100) / 100.0,
                round(confAddition * 100) / 100.0
            )
        } else Pair(0.0, 0.0)
    }

    private fun nanoToMilli(time: Double): Double {
        return time / 1000000

    }

    companion object {
        const val WARM_UP_RESULT_SIZE: Int = 10
    }
}

