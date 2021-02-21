package com.odnovolov.forgetmenot.presentation.screen.helparticle

import androidx.fragment.app.Fragment
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.screen.helparticle.articlecontent.*

enum class HelpArticle(
    val titleRes: Int,
    val iconRes: Int,
    val createFragment: () -> Fragment
) {
    ImportOfFile(
        R.string.help_article_title_import_of_file,
        R.drawable.ic_outline_description_24,
        { ImportOfFileHelpArticleFragment() }),
    Exercise(
        R.string.help_article_title_exercise,
        R.drawable.ic_outline_school_24,
        { ExerciseHelpArticleFragment() }),
    TestingMethods(
        R.string.help_article_title_testing_methods,
        R.drawable.ic_round_rule_24,
        { TestingMethodsArticleFragment() }),
    GradeAndIntervals(
        R.string.help_article_title_grade_and_intervals,
        R.drawable.ic_round_trending_up_24,
        { GradeAndIntervalsHelpArticleFragment() }),
    Pronunciation(
        R.string.help_article_title_pronunciation,
        R.drawable.ic_outline_sms_24,
        { PronunciationHelpArticleFragment() }),
    QuestionDisplay(
        R.string.help_article_title_question_display,
        R.drawable.ic_outline_preview_24,
        { QuestionDisplayArticleFragment() }),
    MotivationalTimer(
        R.string.help_article_title_motivational_timer,
        R.drawable.ic_round_timer_24,
        { MotivationalTimerHelpArticleFragment() }),
    WalkingMode(
        R.string.help_article_title_walking_mode,
        R.drawable.ic_round_directions_walk_24,
        { WalkingModeHelpArticleFragment() }),
    AutoplayingCards(
        R.string.help_article_title_autoplaying_cards,
        R.drawable.ic_round_play_circle_outline_24,
        { AutoplayingCardsHelpArticleFragment() }),
    Presets(
        R.string.help_article_title_presets,
        R.drawable.ic_round_tune_24,
        { PresetsHelpArticleFragment() }),
    AdviceOnCompilingDeck(
        R.string.help_article_title_advice_on_compiling_deck,
        R.drawable.ic_outline_info_24,
        { AdviceOnCompilingDeckHelpArticleFragment() }),
    AdviceOnStudying(
        R.string.help_article_title_advice_on_studying,
        R.drawable.ic_outline_info_24,
        { AdviceOnStudyingHelpArticleFragment() }),
}