package com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.FragmentStateRestorer
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.domain.entity.PronunciationEvent.*
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.inflateAsync
import com.odnovolov.forgetmenot.presentation.common.needToCloseDiScope
import com.odnovolov.forgetmenot.presentation.common.setFont
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.DeckSettingsEvent.*
import com.odnovolov.forgetmenot.presentation.screen.intervals.DisplayedInterval
import kotlinx.android.synthetic.main.fragment_deck_settings.*
import kotlinx.coroutines.launch

class DeckSettingsFragment : BaseFragment() {
    init {
        DeckSettingsDiScope.reopenIfClosed()
    }

    private var controller: DeckSettingsController? = null
    private lateinit var viewModel: DeckSettingsViewModel
    private var isInflated = false
    private lateinit var diScope: DeckSettingsDiScope
    lateinit var scrollListener: NestedScrollView.OnScrollChangeListener
    private val fragmentStateRestorer = FragmentStateRestorer(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragmentStateRestorer.interceptSavedState()
        return inflater.inflateAsync(R.layout.fragment_deck_settings, ::onViewInflated)
    }

    private fun onViewInflated() {
        if (viewCoroutineScope != null) {
            isInflated = true
            setupIfReady()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewCoroutineScope!!.launch {
            diScope = DeckSettingsDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            viewModel = diScope.viewModel
            setupIfReady()
        }
    }

    private fun setupIfReady() {
        if (viewCoroutineScope == null || controller == null || !isInflated) return
        fragmentStateRestorer.restoreState()
        presetView.inject(diScope.presetController, diScope.presetViewModel)
        setFonts()
        setupListeners()
        observeViewModel()
    }

    private fun setFonts() {
        generalSectionTitle.setFont(R.font.nunito_extrabold)
        exerciseSectionTitle.setFont(R.font.nunito_extrabold)
        autoplaySectionTitle.setFont(R.font.nunito_extrabold)
        randomOrderTitle.setFont(R.font.nunito_bold)
        selectedRandomOrderTextView.setFont(R.font.nunito_bold)
        pronunciationTitle.setFont(R.font.nunito_bold)
        selectedPronunciationTextView.setFont(R.font.nunito_bold)
        cardInversionTitle.setFont(R.font.nunito_bold)
        selectedCardInversionTextView.setFont(R.font.nunito_bold)
        questionDisplayTitle.setFont(R.font.nunito_bold)
        selectedQuestionDisplayTextView.setFont(R.font.nunito_bold)
        testingMethodTitle.setFont(R.font.nunito_bold)
        selectedTestingMethodTextView.setFont(R.font.nunito_bold)
        intervalsTitle.setFont(R.font.nunito_bold)
        selectedIntervalsTextView.setFont(R.font.nunito_bold)
        motivationalTimerTitle.setFont(R.font.nunito_bold)
        selectedMotivationalTimerTextView.setFont(R.font.nunito_bold)
        pronunciationPlanTitle.setFont(R.font.nunito_bold)
        selectedPronunciationPlanTextView.setFont(R.font.nunito_bold)
    }

    private fun setupListeners() {
        randomButton.setOnClickListener {
            controller?.dispatch(RandomOrderSwitchToggled)
        }
        testingMethodButton.setOnClickListener {
            controller?.dispatch(TestingMethodButtonClicked)
        }
        intervalsButton.setOnClickListener {
            controller?.dispatch(IntervalsButtonClicked)
        }
        pronunciationButton.setOnClickListener {
            controller?.dispatch(PronunciationButtonClicked)
        }
        questionDisplayButton.setOnClickListener {
            controller?.dispatch(QuestionDisplayButtonClicked)
        }
        cardInversionButton.setOnClickListener {
            controller?.dispatch(CardInversionButtonClicked)
        }
        pronunciationPlanButton.setOnClickListener {
            controller?.dispatch(PronunciationPlanButtonClicked)
        }
        motivationalTimerButton.setOnClickListener {
            controller?.dispatch(MotivationalTimerButtonClicked)
        }
        scrollView.setOnScrollChangeListener(scrollListener)
        scrollListener.onScrollChange(scrollView, 0, 0, 0, 0)
    }

    private fun observeViewModel() {
        with(viewModel) {
            randomOrder.observe { randomOrder: Boolean ->
                selectedRandomOrderTextView.text = getString(
                    if (randomOrder)
                        R.string.on else
                        R.string.off
                )
            }
            selectedTestMethod.observe { selectedTestMethod ->
                selectedTestingMethodTextView.setText(
                    when (selectedTestMethod) {
                        TestingMethod.Off -> R.string.testing_method_without_testing
                        TestingMethod.Manual -> R.string.testing_method_self_testing
                        TestingMethod.Quiz -> R.string.testing_method_testing_with_variants
                        TestingMethod.Entry -> R.string.testing_method_spell_check
                    }
                )
            }
            intervalScheme.observe { intervalScheme: IntervalScheme? ->
                selectedIntervalsTextView.text = composeIntervalSchemeDisplayText(intervalScheme)
            }
            pronunciation.observe { pronunciation: Pronunciation ->
                selectedPronunciationTextView.text = composePronunciationDisplayText(pronunciation)
            }
            isQuestionDisplayed.observe { isQuestionDisplayed: Boolean ->
                selectedQuestionDisplayTextView.text = getString(
                    if (isQuestionDisplayed)
                        R.string.on else
                        R.string.off
                )
            }
            selectedCardInversion.observe { selectedCardInversion: CardInversion ->
                selectedCardInversionTextView.setText(
                    when (selectedCardInversion) {
                        CardInversion.Off -> R.string.item_card_inversion_off
                        CardInversion.On -> R.string.item_card_inversion_on
                        CardInversion.EveryOtherLap -> R.string.item_card_inversion_every_other_lap
                    }
                )
            }
            pronunciationPlan.observe { pronunciationPlan: PronunciationPlan ->
                selectedPronunciationPlanTextView.text =
                    composePronunciationPlanDisplayText(pronunciationPlan)
            }
            timeForAnswer.observe { timeForAnswer: Int ->
                selectedMotivationalTimerTextView.text =
                    if (timeForAnswer == NOT_TO_USE_TIMER)
                        getString(R.string.off) else
                        getString(R.string.time_for_answer, timeForAnswer)
            }
        }
    }

    private fun composeIntervalSchemeDisplayText(intervalScheme: IntervalScheme?): String {
        return if (intervalScheme == null) {
            getString(R.string.off)
        } else {
            intervalScheme.intervals.joinToString(separator = "  ") { interval: Interval ->
                DisplayedInterval.fromDateTimeSpan(interval.value)
                    .getAbbreviation(requireContext())
            }
        }
    }

    private fun composePronunciationDisplayText(pronunciation: Pronunciation): String {
        return buildString {
            append(
                pronunciation.questionLanguage?.displayLanguage
                    ?: getString(R.string.default_language)
            )
            if (pronunciation.questionAutoSpeak) {
                append(" (A)")
            }
            append("  |  ")
            append(
                pronunciation.answerLanguage?.displayLanguage
                    ?: getString(R.string.default_language)
            )
            if (pronunciation.answerAutoSpeak) {
                append(" (A)")
            }
        }
    }

    private fun composePronunciationPlanDisplayText(pronunciationPlan: PronunciationPlan): String {
        return pronunciationPlan.pronunciationEvents
            .joinToString(separator = "  ") { pronunciationEvent: PronunciationEvent ->
                when (pronunciationEvent) {
                    SpeakQuestion -> getString(R.string.speak_event_abbr_speak_question)
                    SpeakAnswer -> getString(R.string.speak_event_abbr_speak_answer)
                    is Delay -> getString(
                        R.string.speak_event_abbr_delay,
                        pronunciationEvent.timeSpan.seconds.toInt()
                    )
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isInflated = false
    }

    override fun onDestroy() {
        super.onDestroy()
        if (needToCloseDiScope()) {
            DeckSettingsDiScope.close()
        }
    }
}