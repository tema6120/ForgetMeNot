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
import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticleDiScope
import com.odnovolov.forgetmenot.presentation.screen.intervals.IntervalsDiScope
import com.odnovolov.forgetmenot.presentation.screen.intervals.modifyinterval.ModifyIntervalDiScope
import com.odnovolov.forgetmenot.presentation.screen.pronunciation.PronunciationDiScope
import com.odnovolov.forgetmenot.presentation.screen.player.PlayerDiScope
import com.odnovolov.forgetmenot.presentation.screen.cardfilterforautoplay.CardFilterForAutoplayDiScope
import com.odnovolov.forgetmenot.presentation.screen.cardfilterforautoplay.lasttested.LastTestedFilterDiScope
import com.odnovolov.forgetmenot.presentation.screen.cardinversion.CardInversionDiScope
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.renamedeck.RenameDeckDiScope
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

    fun navigateToDeckEditorFromExercise(createDiScope: () -> DeckEditorDiScope) {
        DeckEditorDiScope.open(createDiScope)
        navigate(R.id.exercise_to_deck_editor)
    }

    fun navigateToCardsEditorFromExercise(createDiScope: () -> CardsEditorDiScope) {
        CardsEditorDiScope.open(createDiScope)
        navigate(R.id.exercise_to_cards_editor)
    }

    fun navigateToSearchFromExercise(createDiScope: () -> SearchDiScope) {
        SearchDiScope.open(createDiScope)
        navigate(R.id.exercise_to_search)
    }

    fun navigateToWalkingModeSettingsFromExercise() {
        navigate(R.id.exercise_to_walking_mode_settings)
    }

    fun navigateToHelpArticleFromExercise(createDiScope: () -> HelpArticleDiScope) {
        HelpArticleDiScope.open(createDiScope)
        navigate(R.id.exercise_to_help_article)
    }

    fun navigateToCardsEditorFromNavHost(createDiScope: () -> CardsEditorDiScope) {
        CardsEditorDiScope.open(createDiScope)
        navigate(R.id.nav_host_to_cards_editor)
    }

    fun navigateToDeckEditorFromNavHost(createDiScope: () -> DeckEditorDiScope) {
        DeckEditorDiScope.open(createDiScope)
        navigate(R.id.nav_host_to_deck_editor)
    }

    fun showRenameDeckDialogFromNavHost(createDiScope: () -> RenameDeckDiScope) {
        RenameDeckDiScope.open(createDiScope)
        navigate(R.id.nav_host_shows_rename_deck_dialog)
    }

    fun showRenameDeckDialogFromDeckEditor(createDiScope: () -> RenameDeckDiScope) {
        RenameDeckDiScope.open(createDiScope)
        navigate(R.id.deck_editor_shows_rename_deck_dialog)
    }

    fun navigateToDeckEditorFromCardsEditor(createDiScope: () -> DeckEditorDiScope) {
        DeckEditorDiScope.open(createDiScope)
        navigate(R.id.cards_editor_to_deck_editor)
    }

    fun navigateToHelpArticleFromCardsEditor(createDiScope: () -> HelpArticleDiScope) {
        HelpArticleDiScope.open(createDiScope)
        navigate(R.id.cards_editor_to_help_article)
    }

    fun navigateToIntervals(createDiScope: () -> IntervalsDiScope) {
        IntervalsDiScope.open(createDiScope)
        navigate(R.id.deck_editor_to_intervals)
    }

    fun showModifyIntervalDialog(createDiScope: () -> ModifyIntervalDiScope) {
        ModifyIntervalDiScope.open(createDiScope)
        navigate(R.id.show_modify_interval_dialog)
    }

    fun navigateToHelpArticleFromIntervals(createDiScope: () -> HelpArticleDiScope) {
        HelpArticleDiScope.open(createDiScope)
        navigate(R.id.intervals_to_help_article)
    }

    fun navigateToPronunciation(
        createExampleExerciseDiScope: () -> ExampleExerciseDiScope,
        createPronunciationDiScope: () -> PronunciationDiScope
    ) {
        ExampleExerciseDiScope.open(createExampleExerciseDiScope)
        PronunciationDiScope.open(createPronunciationDiScope)
        navigate(R.id.deck_editor_to_pronunciation)
    }

    fun navigateToHelpArticleFromPronunciation(createDiScope: () -> HelpArticleDiScope) {
        HelpArticleDiScope.open(createDiScope)
        navigate(R.id.pronunciation_to_help_article)
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

    fun navigateToHelpArticleFromQuestionDisplay(createDiScope: () -> HelpArticleDiScope) {
        HelpArticleDiScope.open(createDiScope)
        navigate(R.id.question_display_to_help_article)
    }

    fun navigateToTestingMethod(
        createExampleExerciseDiScope: () -> ExampleExerciseDiScope,
        createTestingMethodDiScope: () -> TestingMethodDiScope
    ) {
        ExampleExerciseDiScope.open(createExampleExerciseDiScope)
        TestingMethodDiScope.open(createTestingMethodDiScope)
        navigate(R.id.deck_editor_to_testing_method)
    }

    fun navigateToHelpArticleFromTestingMethod(createDiScope: () -> HelpArticleDiScope) {
        HelpArticleDiScope.open(createDiScope)
        navigate(R.id.testing_method_to_help_article)
    }

    fun navigateToPronunciationPlan(
        createExamplePlayerDiScope: () -> ExamplePlayerDiScope,
        createPronunciationPlanDiScope: () -> PronunciationPlanDiScope
    ) {
        ExamplePlayerDiScope.open(createExamplePlayerDiScope)
        PronunciationPlanDiScope.open(createPronunciationPlanDiScope)
        navigate(R.id.deck_editor_to_pronunciation_plan)
    }

    fun showPronunciationEventDialog() {
        navigate(R.id.show_pronunciation_event_dialog)
    }

    fun navigateToHelpArticleFromPronunciationPlan(createDiScope: () -> HelpArticleDiScope) {
        HelpArticleDiScope.open(createDiScope)
        navigate(R.id.pronunciation_plan_to_help_article)
    }

    fun navigateToMotivationalTimer(
        createExampleExerciseDiScope: () -> ExampleExerciseDiScope,
        createMotivationalTimerDiScope: () -> MotivationalTimerDiScope
    ) {
        ExampleExerciseDiScope.open(createExampleExerciseDiScope)
        MotivationalTimerDiScope.open(createMotivationalTimerDiScope)
        navigate(R.id.deck_editor_to_motivational_timer)
    }

    fun navigateToHelpArticleFromMotivationalTimer(createDiScope: () -> HelpArticleDiScope) {
        HelpArticleDiScope.open(createDiScope)
        navigate(R.id.motivational_timer_to_help_article)
    }

    fun navigateToHelpArticleFromDeckEditor(createDiScope: () -> HelpArticleDiScope) {
        HelpArticleDiScope.open(createDiScope)
        navigate(R.id.deck_editor_to_help_article)
    }

    fun navigateToSearchFromDeckSetup(createDiScope: () -> SearchDiScope) {
        SearchDiScope.open(createDiScope)
        navigate(R.id.deck_editor_to_search)
    }

    fun navigateToCardsEditorFromSearch(createDiScope: () -> CardsEditorDiScope) {
        CardsEditorDiScope.open(createDiScope)
        navigate(R.id.search_to_cards_editor)
    }

    fun navigateToCardsEditorFromDeckEditor(createDiScope: () -> CardsEditorDiScope) {
        CardsEditorDiScope.open(createDiScope)
        navigate(R.id.deck_editor_to_cards_editor)
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

    fun navigateToHelpArticleFromPlayer(createDiScope: () -> HelpArticleDiScope) {
        HelpArticleDiScope.open(createDiScope)
        navigate(R.id.player_to_help_article)
    }

    fun navigateToWalkingModeSettingsFromNavHost() {
        navigate(R.id.nav_host_to_walking_mode_settings)
    }

    fun navigateToHelpArticleFromWalkingModeSettings(createDiScope: () -> HelpArticleDiScope) {
        HelpArticleDiScope.open(createDiScope)
        navigate(R.id.walking_mode_settings_to_help_article)
    }

    fun navigateToHelpArticleFromNavHost(createDiScope: () -> HelpArticleDiScope) {
        HelpArticleDiScope.open(createDiScope)
        navigate(R.id.nav_host_to_help_article)
    }

    fun navigateToWalkingModeSettingsFromHelpArticle() {
        navigate(R.id.help_article_to_walking_mode_settings)
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