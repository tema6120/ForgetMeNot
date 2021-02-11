package com.odnovolov.forgetmenot.presentation.common

import android.util.Log

object Stopwatch {
    private val startTimes: MutableMap<String, Long> = HashMap()

    fun start(name: String = DEFAULT_NAME) {
        startTimes[name] = System.nanoTime()
    }

    fun stop(name: String = DEFAULT_NAME) {
        val stopTime = System.nanoTime()
        val startTime = startTimes.remove(name) ?: return
        val elapsedTime = stopTime - startTime
        val formattedTime: String = formatTime(elapsedTime)
        Log.d("odnovolov", "elapsedTime ($name) = $formattedTime")
    }

    inline fun <R> measure(name: String = DEFAULT_NAME, crossinline block: () -> R): R {
        val startTime = System.nanoTime()
        val result = block()
        val stopTime = System.nanoTime()
        val elapsedTime = stopTime - startTime
        val formattedTime: String = formatTime(elapsedTime)
        Log.d("odnovolov", "elapsedTime ($name) = $formattedTime")
        return result
    }

    fun formatTime(ns: Long): String {
        val ms: Double = ns / 1_000_000.0
        val digits = when {
            ms > 10 -> 0
            ms > 1 -> 1
            ms > 0.1 -> 2
            ms > 0.01 -> 3
            ms > 0.001 -> 4
            ms > 0.0001 -> 5
            else -> 6
        }
        return "%.${digits}f ms".format(ms)
    }

    const val DEFAULT_NAME = "<NO_NAME>"
}