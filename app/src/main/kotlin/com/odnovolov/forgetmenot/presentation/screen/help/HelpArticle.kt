package com.odnovolov.forgetmenot.presentation.screen.help

import androidx.fragment.app.Fragment
import com.odnovolov.forgetmenot.R

enum class HelpArticle(val titleId: Int, val createFragment: () -> Fragment) {
    AboutForgetMeNot(R.string.help_article_title_about_forgetmenot, { Fragment() }),
    ImportOfDeck(R.string.help_article_title_import_of_deck, { Fragment() }),
    HomeScreen(R.string.help_article_title_home_screen, { Fragment() }),
    Exercise(R.string.help_article_title_exercise, { Fragment() }),
    TestMethods(R.string.help_article_title_test_methods, { Fragment() }),
    LevelOfKnowledgeAndIntervals(
        R.string.help_article_title_level_of_knowledge_and_intervals,
        { Fragment() }),
    Pronunciation(R.string.help_article_title_pronunciation, { Fragment() }),
    QuestionDisplay(R.string.help_article_title_question_display, { Fragment() }),
    MotivationalTimer(R.string.help_article_title_motivational_timer, { Fragment() }),
    WalkingMode(R.string.help_article_title_walking_mode, { Fragment() }),
    Repetition(R.string.help_article_title_repetition, { Fragment() }),
    Presets(R.string.help_article_title_presets, { Fragment() }),
    AdviceOnCompilingDeck(R.string.help_article_title_advice_on_compiling_deck, { Fragment() }),
    AdviceOnStudying(R.string.help_article_title_advice_on_studying, { Fragment() }),
}