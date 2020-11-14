package com.odnovolov.forgetmenot.presentation.common

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.mainactivity.MainActivity
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.CardsEditorDiScope
import com.odnovolov.forgetmenot.presentation.screen.decksetup.DeckSetupDiScope
import com.odnovolov.forgetmenot.presentation.screen.decksetup.decksettings.motivationaltimer.MotivationalTimerDiScope
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseDiScope
import com.odnovolov.forgetmenot.presentation.screen.help.HelpDiScope
import com.odnovolov.forgetmenot.presentation.screen.intervals.IntervalsDiScope
import com.odnovolov.forgetmenot.presentation.screen.intervals.modifyinterval.ModifyIntervalDiScope
import com.odnovolov.forgetmenot.presentation.screen.pronunciation.PronunciationDiScope
import com.odnovolov.forgetmenot.presentation.screen.repetition.RepetitionDiScope
import com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.RepetitionSettingsDiScope
import com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.laps.RepetitionLapsDiScope
import com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.lastanswer.LastAnswerFilterDiScope
import com.odnovolov.forgetmenot.presentation.screen.search.SearchDiScope
import com.odnovolov.forgetmenot.presentation.screen.settings.SettingsDiScope
import com.odnovolov.forgetmenot.presentation.screen.pronunciationplan.PronunciationPlanDiScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class Navigator : ActivityLifecycleCallbacks {
    private var navController: NavController? = null

    fun navigateToExercise(createDiScope: () -> ExerciseDiScope) {
        ExerciseDiScope.open(createDiScope)
        navigate(R.id.nav_host_to_exercise)
    }

    fun navigateToCardsEditorFromExercise(createDiScope: () -> CardsEditorDiScope) {
        CardsEditorDiScope.open(createDiScope)
        navigate(R.id.action_exercise_screen_to_cards_editor_screen)
    }

    fun navigateToSearchFromExercise(createDiScope: () -> SearchDiScope) {
        SearchDiScope.open(createDiScope)
        navigate(R.id.action_exercise_screen_to_search_screen)
    }

    fun navigateToWalkingModeSettingsFromExercise() {
        navigate(R.id.action_exercise_screen_to_walking_mode_settings_screen)
    }

    fun navigateToHelpFromExercise(createDiScope: () -> HelpDiScope) {
        HelpDiScope.open(createDiScope)
        navigate(R.id.action_exercise_screen_to_help_screen)
    }

    fun navigateToCardsEditorFromNavHost(createDiScope: () -> CardsEditorDiScope) {
        CardsEditorDiScope.open(createDiScope)
        navigate(R.id.nav_host_to_cards_editor)
    }

    fun navigateToDeckSetupFromNavHost(createDiScope: () -> DeckSetupDiScope) {
        DeckSetupDiScope.open(createDiScope)
        navigate(R.id.nav_host_to_deck_setup)
    }

    fun navigateToDeckSetupFromCardsEditor(createDiScope: () -> DeckSetupDiScope) {
        DeckSetupDiScope.open(createDiScope)
        navigate(R.id.action_cards_editor_screen_to_deck_setup_screen)
    }

    fun navigateToHelpFromCardsEditor(createDiScope: () -> HelpDiScope) {
        HelpDiScope.open(createDiScope)
        navigate(R.id.action_cards_editor_screen_to_help_screen)
    }

    fun navigateToIntervals(createDiScope: () -> IntervalsDiScope) {
        IntervalsDiScope.open(createDiScope)
        navigate(R.id.action_deck_setup_screen_to_intervals_screen)
    }

    fun showModifyIntervalDialog(createDiScope: () -> ModifyIntervalDiScope) {
        ModifyIntervalDiScope.open(createDiScope)
        navigate(R.id.action_show_modify_interval_dialog)
    }

    fun navigateToHelpFromIntervals(createDiScope: () -> HelpDiScope) {
        HelpDiScope.open(createDiScope)
        navigate(R.id.action_intervals_screen_to_help_screen)
    }

    fun navigateToPronunciation(createDiScope: () -> PronunciationDiScope) {
        PronunciationDiScope.open(createDiScope)
        navigate(R.id.action_deck_setup_screen_to_pronunciation_screen)
    }

    fun navigateToHelpFromPronunciation(createDiScope: () -> HelpDiScope) {
        HelpDiScope.open(createDiScope)
        navigate(R.id.action_pronunciation_screen_to_help_screen)
    }

    fun navigateToPronunciationPlan(createDiScope: () -> PronunciationPlanDiScope) {
        PronunciationPlanDiScope.open(createDiScope)
        navigate(R.id.action_deck_setup_screen_to_pronunciation_plan_screen)
    }

    fun showPronunciationEventDialog() {
        navigate(R.id.action_show_pronunciation_event_dialog)
    }

    fun navigateToHelpFromPronunciationPlan(createDiScope: () -> HelpDiScope) {
        HelpDiScope.open(createDiScope)
        navigate(R.id.action_pronunciation_plan_screen_to_help_screen)
    }

    fun showMotivationalTimerDialog(createDiScope: () -> MotivationalTimerDiScope) {
        MotivationalTimerDiScope.open(createDiScope)
        navigate(R.id.action_show_motivational_timer_dialog)
    }

    fun navigateToHelpFromDeckSetup(createDiScope: () -> HelpDiScope) {
        HelpDiScope.open(createDiScope)
        navigate(R.id.action_deck_setup_screen_to_help_screen)
    }

    fun navigateToSearchFromDeckSetup(createDiScope: () -> SearchDiScope) {
        SearchDiScope.open(createDiScope)
        navigate(R.id.action_deck_setup_screen_to_search_screen)
    }

    fun navigateToCardsEditorFromSearch(createDiScope: () -> CardsEditorDiScope) {
        CardsEditorDiScope.open(createDiScope)
        navigate(R.id.action_search_screen_to_cards_editor_screen)
    }

    fun navigateToCardsEditorFromDeckSetup(createDiScope: () -> CardsEditorDiScope) {
        CardsEditorDiScope.open(createDiScope)
        navigate(R.id.action_deck_setup_screen_to_cards_editor_screen)
    }

    fun navigateToAutoplaySettings(createDiScope: () -> RepetitionSettingsDiScope) {
        RepetitionSettingsDiScope.open(createDiScope)
        navigate(R.id.nav_host_to_repetition_settings)
    }

    fun navigateToHelpFromRepetitionSettings(createDiScope: () -> HelpDiScope) {
        HelpDiScope.open(createDiScope)
        navigate(R.id.action_repetition_settings_screen_to_help_screen)
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

    fun navigateToCardEditorFromRepetition(createDiScope: () -> CardsEditorDiScope) {
        CardsEditorDiScope.open(createDiScope)
        navigate(R.id.action_repetition_screen_to_cards_editor_screen)
    }

    fun navigateToSearchFromRepetition(createDiScope: () -> SearchDiScope) {
        SearchDiScope.open(createDiScope)
        navigate(R.id.action_repetition_screen_to_search_screen)
    }

    fun navigateToHelpFromRepetition(createDiScope: () -> HelpDiScope) {
        HelpDiScope.open(createDiScope)
        navigate(R.id.action_repetition_screen_to_help_screen)
    }

    fun navigateToWalkingModeSettingsFromSettings() {
        navigate(R.id.action_settings_screen_to_walking_mode_settings_screen)
    }

    fun navigateToHelpFromSettings(createDiScope: () -> HelpDiScope) {
        HelpDiScope.open(createDiScope)
        navigate(R.id.action_settings_screen_to_help_screen)
    }

    fun navigateToHelpFromNavHost(createDiScope: () -> HelpDiScope) {
        HelpDiScope.open(createDiScope)
        navigate(R.id.nav_host_to_help)
    }

    fun navigateToWalkingModeSettingsFromWalkingModeArticle() {
        navigate(R.id.action_help_screen_to_walking_mode_settings_screen)
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
            navController = activity.findNavController(R.id.mainActivityHostFragment)
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