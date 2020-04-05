package com.odnovolov.forgetmenot.presentation.screen.decksettings

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
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
import com.odnovolov.forgetmenot.presentation.common.observeText
import com.odnovolov.forgetmenot.presentation.common.preset.PresetFragment
import com.odnovolov.forgetmenot.presentation.common.showSoftInput
import com.odnovolov.forgetmenot.presentation.screen.decksettings.DeckSettingsController.Command.ShowRenameDialogWithText
import kotlinx.android.synthetic.main.dialog_input.view.*
import kotlinx.android.synthetic.main.fragment_deck_settings.*
import org.koin.android.ext.android.getKoin
import org.koin.androidx.viewmodel.scope.viewModel

class DeckSettingsFragment : BaseFragment() {
    private val koinScope = getKoin()
        .getOrCreateScope<DeckSettingsViewModel>(DECK_SETTINGS_SCOPED_ID)
    private val viewModel: DeckSettingsViewModel by koinScope.viewModel(this)
    private val controller: DeckSettingsController by koinScope.inject()
    private lateinit var renameDeckDialog: AlertDialog
    private lateinit var renameDeckEditText: EditText
    private lateinit var chooseTestMethodDialog: Dialog
    private lateinit var testMethodAdapter: ItemAdapter<TestMethodItem>
    private lateinit var chooseCardReverseDialog: Dialog
    private lateinit var cardReverseAdapter: ItemAdapter<CardReverseItem>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initRenameDeckDialog()
        initChooseTestMethodDialog()
        initChooseCardReverseDialog()
        return inflater.inflate(R.layout.fragment_deck_settings, container, false)
    }

    private fun initRenameDeckDialog() {
        val contentView = View.inflate(context, R.layout.dialog_input, null)
        renameDeckEditText = contentView.dialogInput
        renameDeckEditText.observeText(controller::onRenameDeckDialogTextChanged)
        renameDeckDialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.title_rename_deck_dialog)
            .setView(contentView)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                controller.onRenameDeckDialogPositiveButtonClicked()
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
                controller.onSelectedTestMethod(chosenTestMethod)
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
                controller.onSelectedCardReverse(chosenCardReverse)
                chooseCardReverseDialog.dismiss()
            },
            takeAdapter = { cardReverseAdapter = it }
        )
    }

    override fun onAttachFragment(childFragment: Fragment) {
        if (childFragment is PresetFragment) {
            childFragment.controller = koinScope.get()
            childFragment.viewModel = koinScope.get()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        observeViewModel()
        controller.commands.observe(::executeCommand)
    }

    private fun setupView() {
        renameDeckButton.setOnClickListener { controller.onRenameDeckButtonClicked() }
        randomButton.setOnClickListener { controller.onRandomOrderSwitchToggled() }
        testMethodButton.setOnClickListener { chooseTestMethodDialog.show() }
        intervalsButton.setOnClickListener { controller.onIntervalsButtonClicked() }
        pronunciationButton.setOnClickListener { controller.onPronunciationButtonClicked() }
        displayQuestionButton.setOnClickListener { controller.onDisplayQuestionSwitchToggled() }
        cardReverseButton.setOnClickListener { chooseCardReverseDialog.show() }
        speakPlanButton.setOnClickListener { controller.onSpeakPlanButtonClicked() }
    }

    private fun observeViewModel() {
        with(viewModel) {
            deckName.observe(deckNameTextView::setText)
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
                testMethodAdapter.items = testMethods
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
                selectedCardReverseTextView.text = when (selectedCardReverse) {
                    CardReverse.Off -> getString(R.string.card_reverse_label_off)
                    CardReverse.On -> getString(R.string.card_reverse_label_on)
                    CardReverse.EveryOtherLap ->
                        getString(R.string.card_reverse_label_every_other_lap)
                }

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
                cardReverseAdapter.items = items
            }
        }
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

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.run {
            getBundle(STATE_KEY_RENAME_DECK_DIALOG)
                ?.let(renameDeckDialog::onRestoreInstanceState)

            getBundle(STATE_KEY_CHOOSE_TEST_METHOD_DIALOG)
                ?.let(chooseTestMethodDialog::onRestoreInstanceState)

            getBundle(STATE_KEY_CHOOSE_CARD_REVERSE_DIALOG)
                ?.let(chooseCardReverseDialog::onRestoreInstanceState)
        }
    }

    override fun onPause() {
        super.onPause()
        if (!isRemoving) {
            controller.performSaving()
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
    }

    companion object {
        const val STATE_KEY_RENAME_DECK_DIALOG = "renameDeckDialog"
        const val STATE_KEY_CHOOSE_TEST_METHOD_DIALOG = "chooseTestMethodDialog"
        const val STATE_KEY_CHOOSE_CARD_REVERSE_DIALOG = "chooseCardReverseDialog"
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