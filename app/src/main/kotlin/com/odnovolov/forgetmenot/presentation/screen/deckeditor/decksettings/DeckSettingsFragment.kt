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
import com.odnovolov.forgetmenot.presentation.common.isFinishing
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
        setupListeners()
        observeViewModel()
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
                selectedRandomOrderTextView.text =
                    getRandomOrderDisplayText(randomOrder, requireContext())
            }
            pronunciation.observe { pronunciation: Pronunciation ->
                selectedPronunciationTextView.text =
                    getPronunciationDisplayText(pronunciation, requireContext())
            }
            cardInversion.observe { cardInversion: CardInversion ->
                selectedCardInversionTextView.text =
                    getCardInversionDisplayText(cardInversion, requireContext())
            }
            isQuestionDisplayed.observe { isQuestionDisplayed: Boolean ->
                selectedQuestionDisplayTextView.text =
                    getQuestionDisplayDisplayText(isQuestionDisplayed, requireContext())
            }
            testingMethod.observe { testingMethod: TestingMethod ->
                selectedTestingMethodTextView.text =
                    getTestingMethodDisplayText(testingMethod, requireContext())
            }
            intervalScheme.observe { intervalScheme: IntervalScheme? ->
                selectedIntervalsTextView.text =
                    getIntervalsDisplayText(intervalScheme, requireContext())
            }
            grading.observe { grading: Grading ->
                selectedGradingTextView.text =
                    getGradingDisplayText(grading, requireContext())
            }
            timeForAnswer.observe { timeForAnswer: Int ->
                selectedMotivationalTimerTextView.text =
                    getMotivationalTimerDisplayText(timeForAnswer, requireContext())
            }
            pronunciationPlan.observe { pronunciationPlan: PronunciationPlan ->
                selectedPronunciationPlanTextView.text =
                    getPronunciationPlanDisplayText(pronunciationPlan, requireContext())
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isInflated = false
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing()) {
            DeckSettingsDiScope.close()
        }
    }
}