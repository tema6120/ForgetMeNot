package com.odnovolov.forgetmenot.entity

data class ExercisePreference(
    val randomOrder: Boolean = true,
    val pronunciation: Pronunciation = Pronunciation()
)