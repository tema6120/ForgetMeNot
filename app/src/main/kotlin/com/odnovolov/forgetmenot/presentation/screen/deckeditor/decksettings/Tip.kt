package com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings

import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMakerWithRegistry
import com.soywiz.klock.DateTime

class TipState(
    override val id: Long,
    needToShow: Boolean = DEFAULT_NEED_TO_SHOW,
    lastShowedAt: DateTime? = DEFAULT_LAST_SHOWED_AT
) : FlowMakerWithRegistry<TipState>() {
    var needToShow: Boolean by flowMaker(needToShow)
    var lastShowedAt: DateTime? by flowMaker(lastShowedAt)

    override fun copy() = TipState(id, needToShow, lastShowedAt)

    companion object {
        const val DEFAULT_NEED_TO_SHOW = true
        val DEFAULT_LAST_SHOWED_AT: DateTime? = null
    }
}

enum class Tip(
    val stringId: Int,
    val state: TipState
) {
    TipPronunciationScreenSelectLanguages(
        stringId = R.string.tip_pronunciation_screen_select_languages,
        state = TipState(id = 0)
    ),

    TipPronunciationScreenAboutTTS(
        stringId = R.string.tip_pronunciation_screen_about_tts,
        state = TipState(id = 1)
    ),

    TipPronunciationScreenAboutAutoSpeaking(
        stringId = R.string.tip_pronunciation_screen_about_autospeaking,
        state = TipState(id = 2)
    ),

    TipPronunciationScreenAboutSelection(
        stringId = R.string.tip_pronunciation_screen_about_selection,
        state = TipState(id = 3)
    ),

    TipPronunciationScreenAboutBrackets(
        stringId = R.string.tip_pronunciation_screen_about_brackets,
        state = TipState(id = 4)
    ),

    TipCardInversionScreen(
        stringId = R.string.tip_card_inversion_screen,
        state = TipState(id = 5)
    ),

    TipQuestionDisplayScreenWhy(
        stringId = R.string.tip_question_display_screen_why,
        state = TipState(id = 6)
    ),

    TipQuestionDisplayScreenDoNotForgetAutospeaking(
        stringId = R.string.tip_question_display_screen_do_not_forget_autospeaking,
        state = TipState(id = 7)
    ),

    TipTestingMethodScreenWithoutTesting(
        stringId = R.string.tip_testing_method_screen_without_testing,
        state = TipState(id = 8)
    ),

    TipTestingMethodScreenSelfTesting(
        stringId = R.string.tip_testing_method_screen_self_testing,
        state = TipState(id = 9)
    ),

    TipTestingMethodScreenTestingWithVariants(
        stringId = R.string.tip_testing_method_screen_testing_with_variants,
        state = TipState(id = 10)
    ),

    TipTestingMethodScreenSpellCheck(
        stringId = R.string.tip_testing_method_screen_spell_check,
        state = TipState(id = 11)
    ),

    TipIntervalsScreenImportance(
        stringId = R.string.tip_intervals_screen_importance,
        state = TipState(id = 12)
    ),

    TipIntervalsScreenAdjustIntervalScheme(
        stringId = R.string.tip_intervals_screen_adjust_interval_scheme,
        state = TipState(id = 13)
    ),

    TipMotivationalTimerScreenDescription(
        stringId = R.string.tip_motivational_timer_screen_description,
        state = TipState(id = 14)
    ),

    TipMotivationalTimerScreenHowToDoWithThis(
        stringId = R.string.tip_motivational_timer_screen_how_to_do_with_this,
        state = TipState(id = 15)
    ),

    TipPronunciationPlanScreenDescription(
        stringId = R.string.tip_pronunciation_plan_screen_description,
        state = TipState(id = 16)
    ),

    TipPronunciationPlanScreenAboutLongerDelay(
        stringId = R.string.tip_pronunciation_plan_screen_about_longer_delay,
        state = TipState(id = 17)
    ),

    TipPronunciationPlanScreenAboutRepetitionPronunciation(
        stringId = R.string.tip_pronunciation_plan_screen_about_repetition_pronunciation,
        state = TipState(id = 18)
    ),

    TipGradingScreenIndication(
        stringId = R.string.tip_grading_screen_indication,
        state = TipState(id = 19)
    ),

    TipGradingScreenAboutRelationshipWithIntervals(
        stringId = R.string.tip_grading_screen_about_relationship_with_intervals,
        state = TipState(id = 20)
    ),

    TipGradingScreenIndicationAboutManualChange(
        stringId = R.string.tip_grading_screen_about_manual_change,
        state = TipState(id = 21)
    )
}

fun determineTipToShow(candidates: List<Tip>): Tip? {
    return candidates.filter { tip -> tip.state.needToShow }
        .sortedBy { tip: Tip -> tip.state.lastShowedAt }
        .firstOrNull()
        ?.also { tip: Tip -> tip.state.lastShowedAt = DateTime.now() }
}