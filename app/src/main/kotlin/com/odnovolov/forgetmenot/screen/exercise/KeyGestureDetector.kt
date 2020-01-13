package com.odnovolov.forgetmenot.screen.exercise

import com.odnovolov.forgetmenot.screen.exercise.KeyGestureDetector.Gesture.*
import kotlinx.coroutines.*

class KeyGestureDetector(
    private val coroutineScope: CoroutineScope,
    private val onGestureDetect: ((Gesture) -> Unit)
) {
    private var longPressDetectorJob: Job? = null
    private var singlePressDetectorJob: Job? = null
    private var isPressed: Boolean = false

    fun dispatchKeyEvent(isPressed: Boolean) {
        if (this.isPressed == isPressed) return else this.isPressed = isPressed
        if (isPressed) onKeyPressed() else onKeyReleased()
    }

    private fun onKeyPressed() {
        if (singlePressDetectorJob.isActive()) {
            singlePressDetectorJob!!.cancel()
            onGestureDetect.invoke(DOUBLE_PRESS)
        } else {
            launchLongPressDetector()
        }
    }

    private fun onKeyReleased() {
        if (longPressDetectorJob.isActive()) {
            longPressDetectorJob!!.cancel()
            launchSinglePressDetector()
        }
    }

    private fun launchLongPressDetector() {
        longPressDetectorJob = coroutineScope.launch {
            delay(LONG_PRESS_DURATION)
            if (isActive) {
                onGestureDetect.invoke(LONG_PRESS)
            }
        }
    }

    private fun launchSinglePressDetector() {
        singlePressDetectorJob = coroutineScope.launch {
            delay(MAX_DOUBLE_PRESS_INTERVAL)
            if (isActive) {
                onGestureDetect.invoke(SINGLE_PRESS)
            }
        }
    }

    private fun Job?.isActive() = this != null && this.isActive

    enum class Gesture {
        SINGLE_PRESS,
        DOUBLE_PRESS,
        LONG_PRESS
    }

    companion object {
        const val MAX_DOUBLE_PRESS_INTERVAL = 300L
        const val LONG_PRESS_DURATION = 300L
    }
}