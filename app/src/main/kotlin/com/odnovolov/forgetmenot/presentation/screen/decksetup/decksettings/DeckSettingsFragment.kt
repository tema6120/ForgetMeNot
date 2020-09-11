package com.odnovolov.forgetmenot.presentation.screen.decksetup.decksettings

import android.app.Dialog
import android.os.Bundle
import android.os.Looper
import android.os.MessageQueue.IdleHandler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.appcompat.app.AlertDialog
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.customview.ChoiceDialogCreator
import com.odnovolov.forgetmenot.presentation.common.customview.ChoiceDialogCreator.Item
import com.odnovolov.forgetmenot.presentation.common.customview.ChoiceDialogCreator.ItemAdapter
import com.odnovolov.forgetmenot.presentation.common.customview.ChoiceDialogCreator.ItemForm.AsRadioButton
import com.odnovolov.forgetmenot.presentation.common.dp
import com.odnovolov.forgetmenot.presentation.common.inflateAsync
import com.odnovolov.forgetmenot.presentation.common.needToCloseDiScope
import com.odnovolov.forgetmenot.presentation.common.uncover
import com.odnovolov.forgetmenot.presentation.screen.decksetup.decksettings.DeckSettingsController.Command.ShowAutoSpeakOfQuestionIsOffMessage
import com.odnovolov.forgetmenot.presentation.screen.decksetup.decksettings.DeckSettingsEvent.*
import kotlinx.android.synthetic.main.fragment_deck_settings.*
import kotlinx.coroutines.launch

class DeckSettingsFragment : BaseFragment() {
    init {
        DeckSettingsDiScope.reopenIfClosed()
    }

    private var controller: DeckSettingsController? = null
    private lateinit var viewModel: DeckSettingsViewModel
    private lateinit var testMethodDialog: Dialog
    private var testMethodAdapter: ItemAdapter? = null
    private lateinit var cardReverseDialog: Dialog
    private var cardReverseAdapter: ItemAdapter? = null
    private var isInflated = false
    private lateinit var diScope: DeckSettingsDiScope
    private var idleHandler: IdleHandler? = null
    private val autoSpeakIsOffDialog: Dialog by lazy(::createAutoSpeakIsOffDialog)

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
        setupPrimary()
        idleHandler = IdleHandler {
            setupSecondary()
            false
        }
        Looper.myQueue().addIdleHandler(idleHandler!!)
    }

    private fun setupPrimary() {
        requireView().viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    requireView().viewTreeObserver.removeOnGlobalLayoutListener(this)
                    setMaxWidthForText()
                }
            })
        with(viewModel) {
            randomOrder.observe { randomOrder: Boolean ->
                randomOrderSwitch.isChecked = randomOrder
                randomOrderSwitch.uncover()
                selectedRandomOrderTextView.text = getString(
                    if (randomOrder)
                        R.string.on else
                        R.string.off
                )
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
                questionDisplaySwitch.isChecked = isQuestionDisplayed
                questionDisplaySwitch.uncover()
                selectedQuestionDisplayTextView.text = getString(
                    if (isQuestionDisplayed)
                        R.string.on else
                        R.string.off
                )
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
            pronunciationPlan.observe { pronunciationPlan: PronunciationPlan ->
                selectedPronunciationPlanTextView.text = when {
                    pronunciationPlan.isDefault() -> getString(R.string.default_name)
                    pronunciationPlan.isIndividual() -> getString(R.string.individual_name)
                    else -> "'${pronunciationPlan.name}'"
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

    private fun setMaxWidthForText() {
        val rootViewWidth = requireView().width
        testMethodTitle.maxWidth = rootViewWidth - 72.dp
        selectedTestMethodTextView.maxWidth = rootViewWidth - 72.dp
        intervalsTitle.maxWidth = rootViewWidth - 96.dp
        selectedIntervalsTextView.maxWidth = rootViewWidth - 96.dp
        pronunciationTitle.maxWidth = rootViewWidth - 96.dp
        selectedPronunciationTextView.maxWidth = rootViewWidth - 96.dp
        questionDisplayTitle.maxWidth = rootViewWidth - 72.dp - questionDisplaySwitch.width
        selectedQuestionDisplayTextView.maxWidth =
            rootViewWidth - 72.dp - questionDisplaySwitch.width
        pronunciationPlanTitle.maxWidth = rootViewWidth - 96.dp
        selectedPronunciationPlanTextView.maxWidth = rootViewWidth - 96.dp
        timeForAnswerTitle.maxWidth = rootViewWidth - 72.dp
        selectedTimeForAnswerTextView.maxWidth = rootViewWidth - 72.dp
    }

    private fun setupSecondary() {
        initChooseTestMethodDialog()
        initChooseCardReverseDialog()
        setupListeners()
        observeViewModelSecondary()
        controller!!.commands.observe(::executeCommand)
    }

    private fun initChooseTestMethodDialog() {
        testMethodDialog = ChoiceDialogCreator.create(
            context = requireContext(),
            title = getString(R.string.title_test_method_dialog),
            itemForm = AsRadioButton,
            onItemClick = { item: Item ->
                item as TestMethodItem
                val chosenTestMethod = item.testMethod
                controller?.dispatch(TestMethodIsSelected(chosenTestMethod))
                testMethodDialog.dismiss()
            },
            takeAdapter = { testMethodAdapter = it }
        )
        dialogTimeCapsule.register("testMethodDialog", testMethodDialog)
    }

    private fun initChooseCardReverseDialog() {
        cardReverseDialog = ChoiceDialogCreator.create(
            context = requireContext(),
            title = getString(R.string.title_card_reverse_dialog),
            itemForm = AsRadioButton,
            onItemClick = { item: Item ->
                item as CardReverseItem
                val chosenCardReverse = item.cardReverse
                controller?.dispatch(CardReverseIsSelected(chosenCardReverse))
                cardReverseDialog.dismiss()
            },
            takeAdapter = { cardReverseAdapter = it }
        )
        dialogTimeCapsule.register("cardReverseDialog", cardReverseDialog)
    }

    private fun setupListeners() {
        randomButton.setOnClickListener {
            controller?.dispatch(RandomOrderSwitchToggled)
        }
        testMethodButton.setOnClickListener {
            testMethodDialog.show()
        }
        intervalsButton.setOnClickListener {
            controller?.dispatch(IntervalsButtonClicked)
        }
        pronunciationButton.setOnClickListener {
            controller?.dispatch(PronunciationButtonClicked)
        }
        displayQuestionButton.setOnClickListener {
            controller?.dispatch(DisplayQuestionSwitchToggled)
        }
        cardReverseButton.setOnClickListener {
            cardReverseDialog.show()
        }
        pronunciationPlanButton.setOnClickListener {
            controller?.dispatch(PronunciationPlanButtonClicked)
        }
        timeForAnswerButton.setOnClickListener {
            controller?.dispatch(TimeForAnswerButtonClicked)
        }
        testMethodHelpButton.setOnClickListener {
            controller?.dispatch(TestMethodHelpButtonClicked)
        }
        intervalsHelpButton.setOnClickListener {
            controller?.dispatch(IntervalsHelpButtonClicked)
        }
        pronunciationHelpButton.setOnClickListener {
            controller?.dispatch(PronunciationHelpButtonClicked)
        }
        questionDisplayHelpButton.setOnClickListener {
            controller?.dispatch(QuestionDisplayHelpButtonClicked)
        }
        pronunciationPlanHelpButton.setOnClickListener {
            controller?.dispatch(PronunciationPlanHelpButtonClicked)
        }
        motivationalTimerHelpButton.setOnClickListener {
            controller?.dispatch(MotivationalTimerHelpButtonClicked)
        }
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

    private fun executeCommand(command: DeckSettingsController.Command) {
        when (command) {
            ShowAutoSpeakOfQuestionIsOffMessage -> autoSpeakIsOffDialog.show()
        }
    }

    private fun createAutoSpeakIsOffDialog(): Dialog {
        return AlertDialog.Builder(requireContext())
            .setView(R.layout.dialog_auto_speak_is_off)
            .setPositiveButton(android.R.string.ok, null)
            .create()
            .also { dialog -> dialogTimeCapsule.register("dialog_auto_speak_is_off", dialog) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        idleHandler?.let { idleHandler -> Looper.myQueue().removeIdleHandler(idleHandler) }
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