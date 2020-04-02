package com.odnovolov.forgetmenot.presentation.common

interface Navigator {
    fun navigateToExercise()
    fun navigateToEditCard()
    fun navigateToDeckSettings()
    fun navigateToIntervals()
    fun navigateToPronunciation()
    fun navigateToSpeakPlan()
    fun showSpeakEventDialog()
    fun navigateToRepetitionSettings()
    fun showLastAnswerFilterDialog()
    fun showRepetitionLapsDialog()
    fun navigateToRepetition()
    fun navigateToSettings()
    fun navigateToWalkingModeSettings()
    fun navigateUp()
}