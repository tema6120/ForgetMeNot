package com.odnovolov.forgetmenot.presentation.common

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.mainactivity.MainActivity
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.CardsEditorDiScope
import com.odnovolov.forgetmenot.presentation.screen.decksettings.motivationaltimer.MotivationalTimerDiScope
import com.odnovolov.forgetmenot.presentation.screen.decksetup.DeckSetupDiScope
import com.odnovolov.forgetmenot.presentation.screen.ongoingcardeditor.OngoingCardEditorDiScope
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseDiScope
import com.odnovolov.forgetmenot.presentation.screen.intervals.IntervalsDiScope
import com.odnovolov.forgetmenot.presentation.screen.intervals.modifyinterval.ModifyIntervalDiScope
import com.odnovolov.forgetmenot.presentation.screen.pronunciation.PronunciationDiScope
import com.odnovolov.forgetmenot.presentation.screen.repetition.RepetitionDiScope
import com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.RepetitionSettingsDiScope
import com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.laps.RepetitionLapsDiScope
import com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.lastanswer.LastAnswerFilterDiScope
import com.odnovolov.forgetmenot.presentation.screen.settings.SettingsDiScope
import com.odnovolov.forgetmenot.presentation.screen.speakplan.SpeakPlanDiScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class Navigator : ActivityLifecycleCallbacks {
    private var navController: NavController? = null

    fun navigateToExercise(createDiScope: () -> ExerciseDiScope) {
        ExerciseDiScope.open(createDiScope)
        navigate(R.id.action_home_screen_to_exercise_screen)
    }

    fun navigateToCardEditorFromExercise(createDiScope: () -> OngoingCardEditorDiScope) {
        OngoingCardEditorDiScope.open(createDiScope)
        navigate(R.id.action_exercise_screen_to_ongoing_card_editor_screen)
    }

    fun navigateToDeckSetup(createDeckSetupDiScope: () -> DeckSetupDiScope) {
        DeckSetupDiScope.open(createDeckSetupDiScope)
        navigate(R.id.action_home_screen_to_deck_settings_screen)
    }

    fun navigateToIntervals(createDiScope: () -> IntervalsDiScope) {
        IntervalsDiScope.open(createDiScope)
        navigate(R.id.action_deck_setup_screen_to_intervals_screen)
    }

    fun showModifyIntervalDialog(createDiScope: () -> ModifyIntervalDiScope) {
        ModifyIntervalDiScope.open(createDiScope)
        navigate(R.id.action_show_modify_interval_dialog)
    }

    fun navigateToPronunciation(createDiScope: () -> PronunciationDiScope) {
        PronunciationDiScope.open(createDiScope)
        navigate(R.id.action_deck_setup_screen_to_pronunciation_screen)
    }

    fun navigateToSpeakPlan(createDiScope: () -> SpeakPlanDiScope) {
        SpeakPlanDiScope.open(createDiScope)
        navigate(R.id.action_deck_setup_screen_to_speak_plan_screen)
    }

    fun showSpeakEventDialog() {
        navigate(R.id.action_show_speak_event_dialog)
    }

    fun showMotivationalTimerDialog(createDiScope: () -> MotivationalTimerDiScope) {
        MotivationalTimerDiScope.open(createDiScope)
        navigate(R.id.action_show_motivational_timer_dialog)
    }

    fun navigateToCardsEditor(createDiScope: () -> CardsEditorDiScope) {
        CardsEditorDiScope.open(createDiScope)
        navigate(R.id.action_deck_setup_screen_to_cards_editor_screen)
    }

    fun navigateToRepetitionSettings(createDiScope: () -> RepetitionSettingsDiScope) {
        RepetitionSettingsDiScope.open(createDiScope)
        navigate(R.id.action_home_screen_to_repetition_settings_screen)
    }

    fun showLastAnswerFilterDialog(createDiScope: () -> LastAnswerFilterDiScope) {
        LastAnswerFilterDiScope.open(createDiScope)
        navigate(R.id.action_show_last_answer_filter_dialog)
    }

    fun showRepetitionLapsDialog(createDiScope: () -> RepetitionLapsDiScope) {
        RepetitionLapsDiScope.open(createDiScope)
        navigate(R.id.action_show_repetition_last_dialog)
    }

    fun navigateToRepetition(createDiScope: () -> RepetitionDiScope) {
        RepetitionDiScope.open(createDiScope)
        navigate(R.id.action_repetition_settings_screen_to_repetition_screen)
    }

    fun navigateToCardEditorFromRepetition(createDiScope: () -> OngoingCardEditorDiScope) {
        OngoingCardEditorDiScope.open(createDiScope)
        navigate(R.id.action_repetition_screen_to_ongoing_card_editor_screen)
    }

    fun navigateToSettings(createDiScope: () -> SettingsDiScope) {
        SettingsDiScope.open(createDiScope)
        navigate(R.id.action_home_screen_to_setup_screen)
    }

    fun navigateToWalkingModeSettings() {
        navigate(R.id.action_settings_screen_to_walking_mode_settings_screen)
    }

    fun navigateUp() {
        GlobalScope.launch(Dispatchers.Main.immediate) {
            try {
                navController?.navigateUp()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun navigate(actionId: Int) {
        GlobalScope.launch(Dispatchers.Main.immediate) {
            try {
                navController!!.navigate(actionId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onActivityStarted(activity: Activity) {
        if (activity is MainActivity && navController == null) {
            navController = activity.findNavController(R.id.navHostFragment)
        }
    }

    override fun onActivityDestroyed(activity: Activity) {
        if (activity is MainActivity) {
            navController = null
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityStopped(activity: Activity) {}
}