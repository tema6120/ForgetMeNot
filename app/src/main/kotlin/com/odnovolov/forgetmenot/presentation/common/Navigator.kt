package com.odnovolov.forgetmenot.presentation.common

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.mainactivity.MainActivity
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.CardsEditorDiScope
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorDiScope
import com.odnovolov.forgetmenot.presentation.screen.motivationaltimer.MotivationalTimerDiScope
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseDiScope
import com.odnovolov.forgetmenot.presentation.screen.help.HelpDiScope
import com.odnovolov.forgetmenot.presentation.screen.intervals.IntervalsDiScope
import com.odnovolov.forgetmenot.presentation.screen.intervals.modifyinterval.ModifyIntervalDiScope
import com.odnovolov.forgetmenot.presentation.screen.pronunciation.PronunciationDiScope
import com.odnovolov.forgetmenot.presentation.screen.player.PlayerDiScope
import com.odnovolov.forgetmenot.presentation.screen.cardfilterforautoplay.CardFilterForAutoplayDiScope
import com.odnovolov.forgetmenot.presentation.screen.cardfilterforautoplay.lasttested.LastTestedFilterDiScope
import com.odnovolov.forgetmenot.presentation.screen.cardinversion.CardInversionDiScope
import com.odnovolov.forgetmenot.presentation.screen.exampleexercise.ExampleExerciseDiScope
import com.odnovolov.forgetmenot.presentation.screen.exampleplayer.ExamplePlayerDiScope
import com.odnovolov.forgetmenot.presentation.screen.search.SearchDiScope
import com.odnovolov.forgetmenot.presentation.screen.pronunciationplan.PronunciationPlanDiScope
import com.odnovolov.forgetmenot.presentation.screen.questiondisplay.QuestionDisplayDiScope
import com.odnovolov.forgetmenot.presentation.screen.testingmethod.TestingMethodDiScope
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

    fun navigateToDeckEditorFromNavHost(createDiScope: () -> DeckEditorDiScope) {
        DeckEditorDiScope.open(createDiScope)
        navigate(R.id.nav_host_to_deck_editor)
    }

    fun navigateToDeckSetupFromCardsEditor(createDiScope: () -> DeckEditorDiScope) {
        DeckEditorDiScope.open(createDiScope)
        navigate(R.id.cards_editor_to_deck_editor)
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

    fun navigateToPronunciation(
        createExampleExerciseDiScope: () -> ExampleExerciseDiScope,
        createPronunciationDiScope: () -> PronunciationDiScope
    ) {
        ExampleExerciseDiScope.open(createExampleExerciseDiScope)
        PronunciationDiScope.open(createPronunciationDiScope)
        navigate(R.id.action_deck_setup_screen_to_pronunciation_screen)
    }

    fun navigateToHelpFromPronunciation(createDiScope: () -> HelpDiScope) {
        HelpDiScope.open(createDiScope)
        navigate(R.id.action_pronunciation_screen_to_help_screen)
    }

    fun navigateToCardInversion(
        createExampleExerciseDiScope: () -> ExampleExerciseDiScope,
        createCardInversionDiScope: () -> CardInversionDiScope
    ) {
        ExampleExerciseDiScope.open(createExampleExerciseDiScope)
        CardInversionDiScope.open(createCardInversionDiScope)
        navigate(R.id.deck_editor_to_card_inversion)
    }

    fun navigateToQuestionDisplay(
        createExampleExerciseDiScope: () -> ExampleExerciseDiScope,
        createQuestionDisplayDiScope: () -> QuestionDisplayDiScope
    ) {
        ExampleExerciseDiScope.open(createExampleExerciseDiScope)
        QuestionDisplayDiScope.open(createQuestionDisplayDiScope)
        navigate(R.id.deck_editor_to_question_display)
    }

    fun navigateToHelpFromQuestionDisplay(createDiScope: () -> HelpDiScope) {
        HelpDiScope.open(createDiScope)
        navigate(R.id.question_display_to_help)
    }

    fun navigateToTestingMethod(
        createExampleExerciseDiScope: () -> ExampleExerciseDiScope,
        createTestingMethodDiScope: () -> TestingMethodDiScope
    ) {
        ExampleExerciseDiScope.open(createExampleExerciseDiScope)
        TestingMethodDiScope.open(createTestingMethodDiScope)
        navigate(R.id.deck_editor_to_testing_method)
    }

    fun navigateToHelpFromTestingMethod(createDiScope: () -> HelpDiScope) {
        HelpDiScope.open(createDiScope)
        navigate(R.id.testing_method_to_help)
    }

    fun navigateToPronunciationPlan(
        createExamplePlayerDiScope: () -> ExamplePlayerDiScope,
        createPronunciationPlanDiScope: () -> PronunciationPlanDiScope
    ) {
        ExamplePlayerDiScope.open(createExamplePlayerDiScope)
        PronunciationPlanDiScope.open(createPronunciationPlanDiScope)
        navigate(R.id.action_deck_setup_screen_to_pronunciation_plan_screen)
    }

    fun showPronunciationEventDialog() {
        navigate(R.id.action_show_pronunciation_event_dialog)
    }

    fun navigateToHelpFromPronunciationPlan(createDiScope: () -> HelpDiScope) {
        HelpDiScope.open(createDiScope)
        navigate(R.id.action_pronunciation_plan_screen_to_help_screen)
    }

    fun navigateToMotivationalTimer(
        createExampleExerciseDiScope: () -> ExampleExerciseDiScope,
        createMotivationalTimerDiScope: () -> MotivationalTimerDiScope
    ) {
        ExampleExerciseDiScope.open(createExampleExerciseDiScope)
        MotivationalTimerDiScope.open(createMotivationalTimerDiScope)
        navigate(R.id.deck_editor_to_motivational_timer)
    }

    fun navigateToHelpFromMotivationalTimer(createDiScope: () -> HelpDiScope) {
        HelpDiScope.open(createDiScope)
        navigate(R.id.motivational_timer_to_help)
    }

    fun navigateToHelpFromDeckEditor(createDiScope: () -> HelpDiScope) {
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

    fun navigateToCardsEditorFromDeckEditor(createDiScope: () -> CardsEditorDiScope) {
        CardsEditorDiScope.open(createDiScope)
        navigate(R.id.action_deck_setup_screen_to_cards_editor_screen)
    }

    fun navigateToAutoplaySettings(createDiScope: () -> CardFilterForAutoplayDiScope) {
        CardFilterForAutoplayDiScope.open(createDiScope)
        navigate(R.id.nav_host_to_card_filter_for_autoplay)
    }

    fun showLastTestedFilterDialog(createDiScope: () -> LastTestedFilterDiScope) {
        LastTestedFilterDiScope.open(createDiScope)
        navigate(R.id.show_last_tested_filter_dialog)
    }

    fun navigateToPlayer(createDiScope: () -> PlayerDiScope) {
        PlayerDiScope.open(createDiScope)
        navigate(R.id.card_filter_to_player)
    }

    fun navigateToCardEditorFromPlayer(createDiScope: () -> CardsEditorDiScope) {
        CardsEditorDiScope.open(createDiScope)
        navigate(R.id.player_to_cards_editor)
    }

    fun navigateToSearchFromPlayer(createDiScope: () -> SearchDiScope) {
        SearchDiScope.open(createDiScope)
        navigate(R.id.player_to_search)
    }

    fun navigateToHelpFromPlayer(createDiScope: () -> HelpDiScope) {
        HelpDiScope.open(createDiScope)
        navigate(R.id.player_to_help)
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