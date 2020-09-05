package com.odnovolov.forgetmenot.presentation.screen.help

import androidx.fragment.app.Fragment
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.screen.help.article.*

enum class HelpArticle(val titleId: Int, val createFragment: () -> Fragment) {
    AboutForgetMeNot(
        R.string.help_article_title_about_forgetmenot,
        { AboutForgetMeNotHelpArticleFragment() }),
    DeckSummary(
        R.string.help_article_title_deck_summary,
        { DeckSummaryHelpArticleFragment() }),
    ImportOfDeck(
        R.string.help_article_title_import_of_deck,
        { ImportOfDeckHelpArticleFragment() }),
    Exercise(
        R.string.help_article_title_exercise,
        { ExerciseHelpArticleFragment() }),
    TestMethods(
        R.string.help_article_title_test_methods,
        { TestMethodsArticleFragment() }),
    LevelOfKnowledgeAndIntervals(
        R.string.help_article_title_level_of_knowledge_and_intervals,
        { LevelOfKnowledgeAndIntervalsHelpArticleFragment() }),
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
    Repetition(
        R.string.help_article_title_repetition,
        { RepetitionHelpArticleFragment() }),
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