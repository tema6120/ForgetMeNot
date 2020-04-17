package com.odnovolov.forgetmenot.presentation.screen.decksettings

import android.app.Dialog
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult.*
import com.odnovolov.forgetmenot.domain.isDefault
import com.odnovolov.forgetmenot.domain.isIndividual
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.customview.ChoiceDialogCreator
import com.odnovolov.forgetmenot.presentation.common.customview.ChoiceDialogCreator.Item
import com.odnovolov.forgetmenot.presentation.common.customview.ChoiceDialogCreator.ItemAdapter
import com.odnovolov.forgetmenot.presentation.common.customview.ChoiceDialogCreator.ItemForm.AsRadioButton
import com.odnovolov.forgetmenot.presentation.common.inflateAsync
import com.odnovolov.forgetmenot.presentation.common.needToCloseDiScope
import com.odnovolov.forgetmenot.presentation.common.observeText
import com.odnovolov.forgetmenot.presentation.common.showSoftInput
import com.odnovolov.forgetmenot.presentation.screen.decksettings.DeckSettingsController.Command.ShowRenameDialogWithText
import com.odnovolov.forgetmenot.presentation.screen.decksettings.DeckSettingsEvent.*
import kotlinx.android.synthetic.main.dialog_input.view.*
import kotlinx.android.synthetic.main.fragment_deck_settings.*
import kotlinx.coroutines.launch

class DeckSettingsFragment : BaseFragment() {
    init {
        DeckSettingsDiScope.reopenIfClosed()
    }

    private var controller: DeckSettingsController? = null
    private lateinit var viewModel: DeckSettingsViewModel
    private lateinit var renameDeckDialog: AlertDialog
    private lateinit var renameDeckEditText: EditText
    private lateinit var chooseTestMethodDialog: Dialog
    private var testMethodAdapter: ItemAdapter<TestMethodItem>? = null
    private lateinit var chooseCardReverseDialog: Dialog
    private var cardReverseAdapter: ItemAdapter<CardReverseItem>? = null
    private var isInflated = false
    private var savedInstanceState: Bundle? = null
    private lateinit var diScope: DeckSettingsDiScope

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflateAsync(R.layout.fragment_deck_settings, ::onViewInflated)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewCoroutineScope!!.launch {
            diScope = DeckSettingsDiScope.get()
            controller = diScope.controller
            viewModel = diScope.viewModel
            setupIfReady()
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        this.savedInstanceState = savedInstanceState
    }

    private fun onViewInflated() {
        isInflated = true
        setupIfReady()
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
            deckName.observe(deckNameTextView::setText)
            randomOrder.observe { randomOrder: Boolean ->
                randomOrderSwitch.isChecked = randomOrder
                if (randomOrderSwitch.visibility == INVISIBLE) {
                    randomOrderSwitch.jumpDrawablesToCurrentState()
                    randomOrderSwitch.visibility = VISIBLE
                }
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
                if (displayQuestionSwitch.visibility == INVISIBLE) {
                    displayQuestionSwitch.jumpDrawablesToCurrentState()
                    displayQuestionSwitch.visibility = VISIBLE
                }
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
        }
    }

    private fun setupSecondary() {
        initRenameDeckDialog()
        initChooseTestMethodDialog()
        initChooseCardReverseDialog()
        setupListeners()
        observeViewModelSecondary()
        restoreState()
        controller!!.commands.observe(::executeCommand)
    }

    private fun initRenameDeckDialog() {
        val contentView = View.inflate(context, R.layout.dialog_input, null)
        renameDeckEditText = contentView.dialogInput
        renameDeckEditText.observeText { text: String ->
            controller?.dispatch(RenameDeckDialogTextChanged(text))
        }
        renameDeckDialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.title_rename_deck_dialog)
            .setView(contentView)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                controller?.dispatch(RenameDeckDialogPositiveButtonClicked)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
        renameDeckDialog.setOnShowListener { renameDeckEditText.showSoftInput() }
    }

    private fun initChooseTestMethodDialog() {
        chooseTestMethodDialog = ChoiceDialogCreator.create(
            context = requireContext(),
            title = getString(R.string.title_choose_test_method_dialog),
            itemForm = AsRadioButton,
            onItemClick = { item: TestMethodItem ->
                val chosenTestMethod = item.testMethod
                controller?.dispatch(SelectedTestMethod(chosenTestMethod))
                chooseTestMethodDialog.dismiss()
            },
            takeAdapter = { testMethodAdapter = it }
        )
    }

    private fun initChooseCardReverseDialog() {
        chooseCardReverseDialog = ChoiceDialogCreator.create(
            context = requireContext(),
            title = getString(R.string.title_choose_card_reverse_dialog),
            itemForm = AsRadioButton,
            onItemClick = { item: CardReverseItem ->
                val chosenCardReverse = item.cardReverse
                controller?.dispatch(SelectedCardReverse(chosenCardReverse))
                chooseCardReverseDialog.dismiss()
            },
            takeAdapter = { cardReverseAdapter = it }
        )
    }

    private fun setupListeners() {
        renameDeckButton.setOnClickListener { controller?.dispatch(RenameDeckButtonClicked) }
        randomButton.setOnClickListener { controller?.dispatch(RandomOrderSwitchToggled) }
        testMethodButton.setOnClickListener { chooseTestMethodDialog.show() }
        intervalsButton.setOnClickListener { controller?.dispatch(IntervalsButtonClicked) }
        pronunciationButton.setOnClickListener { controller?.dispatch(PronunciationButtonClicked) }
        displayQuestionButton.setOnClickListener {
            controller?.dispatch(DisplayQuestionSwitchToggled)
        }
        cardReverseButton.setOnClickListener { chooseCardReverseDialog.show() }
        speakPlanButton.setOnClickListener { controller?.dispatch(SpeakPlanButtonClicked) }
    }

    private fun observeViewModelSecondary() {
        with(viewModel) {
            deckNameCheckResult.observe { nameCheckResult: NameCheckResult ->
                renameDeckEditText.error = when (nameCheckResult) {
                    Ok -> null
                    Empty -> getString(R.string.error_message_empty_name)
                    Occupied -> getString(R.string.error_message_occupied_name)
                }
                if (renameDeckDialog.isShowing) {
                    renameDeckDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .isEnabled = nameCheckResult == Ok
                }
            }

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
                testMethodAdapter?.items = testMethods
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
                cardReverseAdapter?.items = items
            }
        }
    }

    private fun restoreState() {
        savedInstanceState?.run {
            getBundle(STATE_KEY_RENAME_DECK_DIALOG)
                ?.let(renameDeckDialog::onRestoreInstanceState)

            getBundle(STATE_KEY_CHOOSE_TEST_METHOD_DIALOG)
                ?.let(chooseTestMethodDialog::onRestoreInstanceState)

            getBundle(STATE_KEY_CHOOSE_CARD_REVERSE_DIALOG)
                ?.let(chooseCardReverseDialog::onRestoreInstanceState)

            getBundle(STATE_KEY_EXERCISE_PREFERENCE_PRESET_VIEW)
                ?.let(presetView::restoreInstanceState)
        }
        savedInstanceState = null
    }

    private fun executeCommand(command: DeckSettingsController.Command) {
        when (command) {
            is ShowRenameDialogWithText -> {
                renameDeckEditText.setText(command.text)
                renameDeckEditText.selectAll()
                renameDeckDialog.show()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (::renameDeckDialog.isInitialized) {
            outState.putBundle(
                STATE_KEY_RENAME_DECK_DIALOG,
                renameDeckDialog.onSaveInstanceState()
            )
        }
        if (::chooseTestMethodDialog.isInitialized) {
            outState.putBundle(
                STATE_KEY_CHOOSE_TEST_METHOD_DIALOG,
                chooseTestMethodDialog.onSaveInstanceState()
            )
        }
        if (::chooseCardReverseDialog.isInitialized) {
            outState.putBundle(
                STATE_KEY_CHOOSE_CARD_REVERSE_DIALOG,
                chooseCardReverseDialog.onSaveInstanceState()
            )
        }
        if (isInflated) {
            outState.putBundle(
                STATE_KEY_EXERCISE_PREFERENCE_PRESET_VIEW,
                presetView.saveInstanceState()
            )
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

    companion object {
        const val STATE_KEY_RENAME_DECK_DIALOG = "STATE_KEY_RENAME_DECK_DIALOG"
        const val STATE_KEY_CHOOSE_TEST_METHOD_DIALOG = "STATE_KEY_CHOOSE_TEST_METHOD_DIALOG"
        const val STATE_KEY_CHOOSE_CARD_REVERSE_DIALOG = "STATE_KEY_CHOOSE_CARD_REVERSE_DIALOG"
        const val STATE_KEY_EXERCISE_PREFERENCE_PRESET_VIEW =
            "STATE_KEY_EXERCISE_PREFERENCE_PRESET_VIEW"
    }
}