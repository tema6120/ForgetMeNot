package com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.NestedScrollView
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.inflateAsync
import com.odnovolov.forgetmenot.presentation.common.needToCloseDiScope
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.DeckSettingsEvent.*
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return if (savedInstanceState == null) {
            inflater.inflateAsync(R.layout.fragment_deck_settings, ::onViewInflated)
        } else {
            inflater.inflate(R.layout.fragment_deck_settings, container, false)
        }
    }

    private fun onViewInflated() {
        if (viewCoroutineScope != null) {
            isInflated = true
            setupIfReady()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState != null) {
            isInflated = true
        }
        viewCoroutineScope!!.launch {
            diScope = DeckSettingsDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            viewModel = diScope.viewModel
            setupIfReady()
        }
    }

    private fun setupIfReady() {
        if (viewCoroutineScope == null || controller == null || !isInflated) return
        presetView.inject(diScope.presetController, diScope.presetViewModel)
        setFonts()
        setupListeners()
        observeViewModel()
    }

    private fun setFonts() {
        val nunitoExtraboldFont = ResourcesCompat.getFont(requireContext(), R.font.nunito_extrabold)
        generalSectionTitle.typeface = nunitoExtraboldFont
        exerciseSectionTitle.typeface = nunitoExtraboldFont
        autoplaySectionTitle.typeface = nunitoExtraboldFont
        val nunitoBoldFont = ResourcesCompat.getFont(requireContext(), R.font.nunito_bold)
        randomOrderTitle.typeface = nunitoBoldFont
        selectedRandomOrderTextView.typeface = nunitoBoldFont
        pronunciationTitle.typeface = nunitoBoldFont
        selectedPronunciationTextView.typeface = nunitoBoldFont
        cardInversionTitle.typeface = nunitoBoldFont
        selectedCardInversionTextView.typeface = nunitoBoldFont
        questionDisplayTitle.typeface = nunitoBoldFont
        selectedQuestionDisplayTextView.typeface = nunitoBoldFont
        testingMethodTitle.typeface = nunitoBoldFont
        selectedTestingMethodTextView.typeface = nunitoBoldFont
        intervalsTitle.typeface = nunitoBoldFont
        selectedIntervalsTextView.typeface = nunitoBoldFont
        motivationalTimerTitle.typeface = nunitoBoldFont
        selectedMotivationalTimerTextView.typeface = nunitoBoldFont
        pronunciationPlanTitle.typeface = nunitoBoldFont
        selectedPronunciationPlanTextView.typeface = nunitoBoldFont
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
            intervalScheme.observe {
                selectedIntervalsTextView.text = when {
                    it == null -> getString(R.string.off)
                    it.isDefault() -> getString(R.string.default_name)
                    it.isIndividual() -> getString(R.string.individual_name)
                    else -> "'${it.name}'"
                }
            }
            pronunciation.observe { pronunciation: Pronunciation ->
                selectedPronunciationTextView.text = buildString {
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
                selectedPronunciationPlanTextView.text = when {
                    pronunciationPlan.isDefault() -> getString(R.string.default_name)
                    pronunciationPlan.isIndividual() -> getString(R.string.individual_name)
                    else -> "'${pronunciationPlan.name}'"
                }
            }
            timeForAnswer.observe { timeForAnswer: Int ->
                selectedMotivationalTimerTextView.text =
                    if (timeForAnswer == 0)
                        getString(R.string.off) else
                        getString(R.string.time_for_answer, timeForAnswer)
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