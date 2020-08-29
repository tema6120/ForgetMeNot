package com.odnovolov.forgetmenot.presentation.screen.help

import androidx.fragment.app.Fragment
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.screen.help.article.QuestionDisplayArticleFragment
import com.odnovolov.forgetmenot.presentation.screen.help.article.SimpleHelpArticleFragment
import com.odnovolov.forgetmenot.presentation.screen.help.article.TestMethodsArticleFragment
import com.odnovolov.forgetmenot.presentation.screen.help.article.WalkingModeHelpArticleFragment
import com.odnovolov.forgetmenot.presentation.screen.help.article.AdviceOnCompilingDeckHelpArticleFragment

enum class HelpArticle(val titleId: Int, val createFragment: () -> Fragment) {
    AboutForgetMeNot(R.string.help_article_title_about_forgetmenot, { Fragment() }),
    ImportOfDeck(
        R.string.help_article_title_import_of_deck,
        { SimpleHelpArticleFragment.create(R.layout.article_import_of_deck) }),
    HomeScreen(R.string.help_article_title_home_screen, { Fragment() }),
    Exercise(R.string.help_article_title_exercise, { Fragment() }),
    TestMethods(R.string.help_article_title_test_methods, { TestMethodsArticleFragment() }),
    LevelOfKnowledgeAndIntervals(
        R.string.help_article_title_level_of_knowledge_and_intervals,
        { Fragment() }),
    Pronunciation(
        R.string.help_article_title_pronunciation,
        { SimpleHelpArticleFragment.create(R.layout.article_pronunciation) }),
    QuestionDisplay(
        R.string.help_article_title_question_display,
        { QuestionDisplayArticleFragment() }),
    MotivationalTimer(
        R.string.help_article_title_motivational_timer,
        { Fragment() }),
    WalkingMode(R.string.help_article_title_walking_mode, { WalkingModeHelpArticleFragment() }),
    Repetition(R.string.help_article_title_repetition, { Fragment() }),
    Presets(R.string.help_article_title_presets, { Fragment() }),
    AdviceOnCompilingDeck(
        R.string.help_article_title_advice_on_compiling_deck,
        { AdviceOnCompilingDeckHelpArticleFragment() }),
    AdviceOnStudying(R.string.help_article_title_advice_on_studying, { Fragment() }),
}