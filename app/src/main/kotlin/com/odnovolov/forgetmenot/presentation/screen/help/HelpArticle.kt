package com.odnovolov.forgetmenot.presentation.screen.help

import androidx.fragment.app.Fragment
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.screen.help.article.*

enum class HelpArticle(val titleId: Int, val createFragment: () -> Fragment) {
    ImportOfDeck(
        R.string.help_article_title_import_of_deck,
        { ImportOfDeckHelpArticleFragment() }),
    Exercise(
        R.string.help_article_title_exercise,
        { ExerciseHelpArticleFragment() }),
    TestingMethods(
        R.string.help_article_title_testing_methods,
        { TestingMethodsArticleFragment() }),
    GradeAndIntervals(
        R.string.help_article_title_grade_and_intervals,
        { GradeAndIntervalsHelpArticleFragment() }),
    Pronunciation(
        R.string.help_article_title_pronunciation,
        { PronunciationHelpArticleFragment() }),
    QuestionDisplay(
        R.string.help_article_title_question_display,
        { QuestionDisplayArticleFragment() }),
    MotivationalTimer(
        R.string.help_article_title_motivational_timer,
        { MotivationalTimerHelpArticleFragment() }),
    WalkingMode(
        R.string.help_article_title_walking_mode,
        { WalkingModeHelpArticleFragment() }),
    AutoplayingCards(
        R.string.help_article_title_autoplaying_cards,
        { AutoplayingCardsHelpArticleFragment() }),
    Presets(
        R.string.help_article_title_presets,
        { PresetsHelpArticleFragment() }),
    AdviceOnCompilingDeck(
        R.string.help_article_title_advice_on_compiling_deck,
        { AdviceOnCompilingDeckHelpArticleFragment() }),
    AdviceOnStudying(
        R.string.help_article_title_advice_on_studying,
        { AdviceOnStudyingHelpArticleFragment() }),
}