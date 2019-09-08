package com.odnovolov.forgetmenot.exercise

import java.util.*

sealed class ExerciseOrder {
    object MoveToNextPosition : ExerciseOrder()
    class Speak(val text: String, val language: Locale?) : ExerciseOrder()
}