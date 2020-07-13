package com.odnovolov.forgetmenot.presentation.screen.decksettings

import android.app.Dialog
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.domain.isDefault
import com.odnovolov.forgetmenot.domain.isIndividual
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.customview.ChoiceDialogCreator
import com.odnovolov.forgetmenot.presentation.common.customview.ChoiceDialogCreator.Item
import com.odnovolov.forgetmenot.presentation.common.customview.ChoiceDialogCreator.ItemAdapter
import com.odnovolov.forgetmenot.presentation.common.customview.ChoiceDialogCreator.ItemForm.AsRadioButton
import com.odnovolov.forgetmenot.presentation.common.inflateAsync
import com.odnovolov.forgetmenot.presentation.common.needToCloseDiScope
import com.odnovolov.forgetmenot.presentation.common.uncover
import com.odnovolov.forgetmenot.presentation.screen.decksettings.DeckSettingsEvent.*
import kotlinx.android.synthetic.main.fragment_deck_settings.*
import kotlinx.coroutines.launch

class DeckSettingsFragment : BaseFragment() {
    init {
        DeckSettingsDiScope.reopenIfClosed()
    }

    private var controller: DeckSettingsController? = null
    private lateinit var viewModel: DeckSettingsViewModel
    private lateinit var chooseTestMethodDialog: Dialog
    private var testMethodAdapter: ItemAdapter? = null
    private lateinit var chooseCardReverseDialog: Dialog
    private var cardReverseAdapter: ItemAdapter? = null
    private var isInflated = false
    private lateinit var diScope: DeckSettingsDiScope

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
        isInflated = true
        setupIfReady()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState != null) {
            isInflated = true
        }
        viewCoroutineScope!!.launch {
            diScope = DeckSettingsDiScope.get()
            controller = diScope.controller
            viewModel = diScope.viewModel
            setupIfReady()
        }
    }

    private fun setupIfReady() {
        if (viewCoroutineScope == null || controller == null || !isInflated) return
        presetView.inject(diScope.presetController, diScope.presetViewModel)
        setupPrimary()
        Looper.myQueue().addIdleHandler {
            setupSecondary()
            false
        }
    }

    private fun setupPrimary() {
        with(viewModel) {
            randomOrder.observe { randomOrder: Boolean ->
                randomOrderSwitch.isChecked = randomOrder
                randomOrderSwitch.uncover()
            }
            selectedTestMethod.observe { selectedTestMethod ->
                selectedTestMethodTextView.text = when (selectedTestMethod) {
                    TestMethod.Off -> getString(R.string.test_method_label_off)
                    TestMethod.Manual -> getString(R.string.test_method_label_manual)
                    TestMethod.Quiz -> getString(R.string.test_method_label_quiz)
                    TestMethod.Entry -> getString(R.string.test_method_label_entry)
                }
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
                selectedPronunciationTextView.text = when {
                    pronunciation.isDefault() -> getString(R.string.default_name)
                    pronunciation.isIndividual() -> getString(R.string.individual_name)
                    else -> "'${pronunciation.name}'"
                }
            }
            isQuestionDisplayed.observe { isQuestionDisplayed: Boolean ->
                displayQuestionSwitch.isChecked = isQuestionDisplayed
                displayQuestionSwitch.uncover()
            }
            selectedCardReverse.observe { selectedCardReverse: CardReverse ->
                selectedCardReverseTextView.text =
                    when (selectedCardReverse) {
                        CardReverse.Off -> getString(R.string.card_reverse_label_off)
                        CardReverse.On -> getString(R.string.card_reverse_label_on)
                        CardReverse.EveryOtherLap ->
                            getString(R.string.card_reverse_label_every_other_lap)
                    }
            }
            speakPlan.observe { speakPlan: SpeakPlan ->
                selectedSpeakPlanTextView.text = when {
                    speakPlan.isDefault() -> getString(R.string.default_name)
                    speakPlan.isIndividual() -> getString(R.string.individual_name)
                    else -> "'${speakPlan.name}'"
                }
            }
            timeForAnswer.observe { timeForAnswer: Int ->
                selectedTimeForAnswerTextView.text =
                    if (timeForAnswer == 0)
                        getString(R.string.off) else
                        getString(R.string.selected_time_for_answer, timeForAnswer)
            }
        }
    }

    private fun setupSecondary() {
        initChooseTestMethodDialog()
        initChooseCardReverseDialog()
        setupListeners()
        observeViewModelSecondary()
    }

    private fun initChooseTestMethodDialog() {
        chooseTestMethodDialog = ChoiceDialogCreator.create(
            context = requireContext(),
            title = getString(R.string.title_choose_test_method_dialog),
            itemForm = AsRadioButton,
            onItemClick = { item: Item ->
                item as TestMethodItem
                val chosenTestMethod = item.testMethod
                controller?.dispatch(SelectedTestMethod(chosenTestMethod))
                chooseTestMethodDialog.dismiss()
            },
            takeAdapter = { testMethodAdapter = it }
        )
        dialogTimeCapsule.register("chooseTestMethodDialog", chooseTestMethodDialog)
    }

    private fun initChooseCardReverseDialog() {
        chooseCardReverseDialog = ChoiceDialogCreator.create(
            context = requireContext(),
            title = getString(R.string.title_choose_card_reverse_dialog),
            itemForm = AsRadioButton,
            onItemClick = { item: Item ->
                item as CardReverseItem
                val chosenCardReverse = item.cardReverse
                controller?.dispatch(SelectedCardReverse(chosenCardReverse))
                chooseCardReverseDialog.dismiss()
            },
            takeAdapter = { cardReverseAdapter = it }
        )
        dialogTimeCapsule.register("chooseCardReverseDialog", chooseCardReverseDialog)
    }

    private fun setupListeners() {
        randomButton.setOnClickListener { controller?.dispatch(RandomOrderSwitchToggled) }
        testMethodButton.setOnClickListener { chooseTestMethodDialog.show() }
        intervalsButton.setOnClickListener { controller?.dispatch(IntervalsButtonClicked) }
        pronunciationButton.setOnClickListener { controller?.dispatch(PronunciationButtonClicked) }
        displayQuestionButton.setOnClickListener {
            controller?.dispatch(DisplayQuestionSwitchToggled)
        }
        cardReverseButton.setOnClickListener { chooseCardReverseDialog.show() }
        speakPlanButton.setOnClickListener { controller?.dispatch(SpeakPlanButtonClicked) }
        timeForAnswerButton.setOnClickListener { controller?.dispatch(TimeForAnswerButtonClicked) }
    }

    private fun observeViewModelSecondary() {
        with(viewModel) {
            selectedTestMethod.observe { selectedTestMethod ->
                val testMethods = TestMethod.values().map {
                    TestMethodItem(
                        testMethod = it,
                        text = when (it) {
                            TestMethod.Off -> getString(R.string.test_method_label_off)
                            TestMethod.Manual -> getString(R.string.test_method_label_manual)
                            TestMethod.Quiz -> getString(R.string.test_method_label_quiz)
                            TestMethod.Entry -> getString(R.string.test_method_label_entry)
                        },
                        isSelected = it === selectedTestMethod
                    )
                }
                testMethodAdapter?.submitList(testMethods)
            }
            selectedCardReverse.observe { selectedCardReverse: CardReverse ->
                val items = CardReverse.values().map { cardReverse: CardReverse ->
                    CardReverseItem(
                        cardReverse = cardReverse,
                        text = when (cardReverse) {
                            CardReverse.Off -> getString(R.string.card_reverse_label_off)
                            CardReverse.On -> getString(R.string.card_reverse_label_on)
                            CardReverse.EveryOtherLap ->
                                getString(R.string.card_reverse_label_every_other_lap)
                        },
                        isSelected = cardReverse === selectedCardReverse
                    )
                }
                cardReverseAdapter?.submitList(items)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isInflated = false
        testMethodAdapter = null
        cardReverseAdapter = null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (needToCloseDiScope()) {
            DeckSettingsDiScope.close()
        }
    }

    data class TestMethodItem(
        val testMethod: TestMethod,
        override val text: String,
        override val isSelected: Boolean
    ) : Item

    data class CardReverseItem(
        val cardReverse: CardReverse,
        override val text: String,
        override val isSelected: Boolean
    ) : Item
}