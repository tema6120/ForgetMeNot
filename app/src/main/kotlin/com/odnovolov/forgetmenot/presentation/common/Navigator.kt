package com.odnovolov.forgetmenot.presentation.common

import REPETITION_LAPS_SCOPE_ID
import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.domain.interactor.repetition.Repetition
import com.odnovolov.forgetmenot.domain.interactor.repetition.RepetitionSettings
import com.odnovolov.forgetmenot.domain.interactor.repetition.RepetitionStateCreator
import com.odnovolov.forgetmenot.presentation.common.preset.PresetDialogState
import com.odnovolov.forgetmenot.presentation.screen.decksettings.DECK_SETTINGS_SCOPED_ID
import com.odnovolov.forgetmenot.presentation.screen.decksettings.DeckSettingsScreenState
import com.odnovolov.forgetmenot.presentation.screen.decksettings.DeckSettingsViewModel
import com.odnovolov.forgetmenot.presentation.screen.editcard.EDIT_CARD_SCOPE_ID
import com.odnovolov.forgetmenot.presentation.screen.editcard.EditCardScreenState
import com.odnovolov.forgetmenot.presentation.screen.editcard.EditCardViewModel
import com.odnovolov.forgetmenot.presentation.screen.exercise.EXERCISE_SCOPE_ID
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseViewModel
import com.odnovolov.forgetmenot.presentation.screen.intervals.INTERVALS_SCOPE_ID
import com.odnovolov.forgetmenot.presentation.screen.intervals.IntervalsViewModel
import com.odnovolov.forgetmenot.presentation.screen.pronunciation.PRONUNCIATION_SCOPE_ID
import com.odnovolov.forgetmenot.presentation.screen.pronunciation.PronunciationViewModel
import com.odnovolov.forgetmenot.presentation.screen.repetition.REPETITION_SCOPE_ID
import com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.REPETITION_SETTINGS_SCOPE_ID
import com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.laps.RepetitionLapsDialogState
import com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.laps.RepetitionLapsViewModel
import org.koin.core.scope.Scope
import org.koin.java.KoinJavaComponent.getKoin

class Navigator : ActivityLifecycleCallbacks {
    private var navController: NavController? = null

    fun navigateToExercise(exerciseState: Exercise.State) {
        val koinScope = getKoin().createScope<ExerciseViewModel>(EXERCISE_SCOPE_ID)
        koinScope.declare(exerciseState, override = true)
        navController?.navigate(R.id.action_home_screen_to_exercise_screen)
    }

    fun navigateToEditCard(editCardScreenState: EditCardScreenState) {
        val koinScope = getKoin().createScope<EditCardViewModel>(EDIT_CARD_SCOPE_ID)
        koinScope.declare(editCardScreenState, override = true)
        navController?.navigate(R.id.action_exercise_screen_to_edit_card_screen)
    }

    fun navigateToDeckSettings(deckSettingsState: DeckSettings.State) {
        val koinScope = getKoin().createScope<DeckSettingsViewModel>(DECK_SETTINGS_SCOPED_ID)
        koinScope.declare(deckSettingsState, override = true)
        koinScope.declare(PresetDialogState(), override = true)
        koinScope.declare(DeckSettingsScreenState(), override = true)
        navController?.navigate(R.id.action_home_screen_to_deck_settings_screen)
    }

    fun navigateToIntervals() {
        val koinScope: Scope = getKoin().createScope<IntervalsViewModel>(INTERVALS_SCOPE_ID)
        koinScope.declare(PresetDialogState(), override = true)
        navController?.navigate(R.id.action_deck_settings_screen_to_intervals_screen)
    }

    fun navigateToPronunciation() {
        val koinScope: Scope = getKoin().createScope<PronunciationViewModel>(PRONUNCIATION_SCOPE_ID)
        koinScope.declare(PresetDialogState(), override = true)
        navController?.navigate(R.id.action_deck_settings_screen_to_pronunciation_screen)
    }

    fun navigateToSpeakPlan() {
        navController?.navigate(R.id.action_deck_settings_screen_to_speak_plan_screen)
    }

    fun showSpeakEventDialog() {
        navController?.navigate(R.id.action_show_speak_event_dialog)
    }

    fun navigateToRepetitionSettings(repetitionCreatorState: RepetitionStateCreator.State) {
        val koinScope = getKoin().createScope<RepetitionSettings>(REPETITION_SETTINGS_SCOPE_ID)
        koinScope.declare(repetitionCreatorState, override = true)
        koinScope.declare(PresetDialogState(), override = true)
        navController?.navigate(R.id.action_home_screen_to_repetition_settings_screen)
    }

    fun showLastAnswerFilterDialog() {
        navController?.navigate(R.id.action_show_last_answer_filter_dialog)
    }

    fun showRepetitionLapsDialog(dialogState: RepetitionLapsDialogState) {
        val koinScope = getKoin().createScope<RepetitionLapsViewModel>(REPETITION_LAPS_SCOPE_ID)
        koinScope.declare(dialogState, override = true)
        navController?.navigate(R.id.action_show_repetition_last_dialog)
    }

    fun navigateToRepetition(repetitionState: Repetition.State) {
        val koinScope = getKoin().createScope<Repetition>(REPETITION_SCOPE_ID)
        koinScope.declare(repetitionState, override = true)
        navController?.navigate(R.id.action_repetition_settings_screen_to_repetition_screen)
    }

    fun navigateToSettings() {
        navController?.navigate(R.id.action_home_screen_to_settings_screen)
    }

    fun navigateToWalkingModeSettings() {
        navController?.navigate(R.id.action_settings_screen_to_walking_mode_settings_screen)
    }

    fun navigateUp() {
        navController?.navigateUp()
    }

    override fun onActivityStarted(activity: Activity) {
        if (activity is MainActivity) {
            navController = activity.findNavController(R.id.navHostFragment)
        }
    }

    override fun onActivityStopped(activity: Activity) {
        if (activity is MainActivity) {
            navController = null
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}