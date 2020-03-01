package com.odnovolov.forgetmenot.presentation.screen.decksettings

import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.EditText
import android.widget.PopupWindow
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.customview.ChoiceDialogCreator
import com.odnovolov.forgetmenot.presentation.common.customview.ChoiceDialogCreator.Item
import com.odnovolov.forgetmenot.presentation.common.customview.ChoiceDialogCreator.ItemAdapter
import com.odnovolov.forgetmenot.presentation.common.customview.ChoiceDialogCreator.ItemForm.AsRadioButton
import com.odnovolov.forgetmenot.presentation.common.customview.InputDialogCreator
import com.odnovolov.forgetmenot.presentation.common.customview.PresetPopupCreator
import com.odnovolov.forgetmenot.presentation.common.customview.PresetPopupCreator.PresetAdapter
import com.odnovolov.forgetmenot.presentation.common.dp
import com.odnovolov.forgetmenot.domain.entity.CardReverse
import com.odnovolov.forgetmenot.domain.entity.ExercisePreference
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult.*
import com.odnovolov.forgetmenot.domain.entity.TestMethod
import com.odnovolov.forgetmenot.domain.entity.TestMethod.*
import com.odnovolov.forgetmenot.domain.isDefault
import com.odnovolov.forgetmenot.domain.isIndividual
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.screen.decksettings.DeckSettingsCommand.SetNamePresetDialogText
import com.odnovolov.forgetmenot.presentation.screen.decksettings.DeckSettingsCommand.SetRenameDeckDialogText
import kotlinx.android.synthetic.main.fragment_deck_settings.*
import org.koin.android.ext.android.getKoin
import org.koin.androidx.viewmodel.scope.viewModel

class DeckSettingsFragment : BaseFragment() {
    private val koinScope = getKoin()
        .getOrCreateScope<DeckSettingsViewModel>(DECK_SETTINGS_SCOPED_ID)
    private val viewModel: DeckSettingsViewModel by koinScope.viewModel(this)
    private val controller: DeckSettingsController by koinScope.inject()
    private lateinit var renameDeckDialog: Dialog
    private lateinit var renameDeckEditText: EditText
    private lateinit var chooseExercisePreferencePopup: PopupWindow
    private lateinit var exercisePreferenceAdapter: PresetAdapter
    private lateinit var namePresetDialog: Dialog
    private lateinit var namePresetEditText: EditText
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
        initChooseExercisePreferencePopup()
        initNamePresetDialog()
        initChooseTestMethodDialog()
        initChooseCardReverseDialog()
        return inflater.inflate(R.layout.fragment_deck_settings, container, false)
    }

    private fun initRenameDeckDialog() {
        renameDeckDialog = InputDialogCreator.create(
            context = requireContext(),
            title = getString(R.string.title_rename_deck_dialog),
            takeEditText = { renameDeckEditText = it },
            onTextChanged = { controller.onRenameDeckDialogTextChanged(it) },
            onPositiveClick = { controller.onRenameDeckDialogPositiveButtonClicked() },
            onNegativeClick = { controller.onRenameDeckDialogNegativeButtonClicked() }
        )
    }

    private fun initChooseExercisePreferencePopup() {
        chooseExercisePreferencePopup = PresetPopupCreator.create(
            context = requireContext(),
            setPresetButtonClickListener = { id: Long? ->
                controller.onSetExercisePreferenceButtonClicked(id!!)
            },
            renamePresetButtonClickListener = { id: Long ->
                controller.onRenameExercisePreferenceButtonClicked(id)
            },
            deletePresetButtonClickListener = { id: Long ->
                controller.onDeleteExercisePreferenceButtonClicked(id)
            },
            addButtonClickListener = {
                controller.onAddNewExercisePreferenceButtonClicked()
            },
            takeAdapter = { exercisePreferenceAdapter = it }
        )
    }

    private fun initNamePresetDialog() {
        namePresetDialog = InputDialogCreator.create(
            context = requireContext(),
            title = getString(R.string.title_exercise_preference_name_input_dialog),
            takeEditText = { namePresetEditText = it },
            onTextChanged = { controller.onNamePresetDialogTextChanged(it) },
            onPositiveClick = { controller.onNamePresetPositiveDialogButtonClicked() },
            onNegativeClick = { controller.onNamePresetNegativeDialogButtonClicked() }
        )
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        observeViewModel()
        controller.commands.observe(::executeCommand)
    }

    private fun setupView() {
        renameDeckButton.setOnClickListener {
            controller.onRenameDeckButtonClicked()
        }
        saveExercisePreferencesButton.setOnClickListener {
            controller.onSaveExercisePreferenceButtonClicked()
        }
        presetNameTextView.setOnClickListener {
            showChooseExercisePreferencePopup()
        }
        randomButton.setOnClickListener {
            controller.onRandomOrderSwitchToggled()
        }
        testMethodButton.setOnClickListener {
            chooseTestMethodDialog.show()
        }
        intervalsButton.setOnClickListener {
            controller.onIntervalsButtonClicked()
        }
        pronunciationButton.setOnClickListener {
            controller.onPronunciationButtonClicked()
        }
        displayQuestionButton.setOnClickListener {
            controller.onDisplayQuestionSwitchToggled()
        }
        cardReverseButton.setOnClickListener {
            chooseCardReverseDialog.show()
        }
    }

    private fun showChooseExercisePreferencePopup() {
        val location = IntArray(2)
        presetNameTextView.getLocationOnScreen(location)
        val x = location[0] + presetNameTextView.width - chooseExercisePreferencePopup.width - 8.dp
        val y = location[1]
        chooseExercisePreferencePopup.showAtLocation(
            presetNameTextView.rootView,
            Gravity.NO_GRAVITY, x, y
        )
    }

    private fun observeViewModel() {
        with(viewModel) {
            deckName.observe(deckNameTextView::setText)
            isRenameDeckDialogVisible.observe { isVisible ->
                renameDeckDialog.run {
                    if (isVisible) show() else dismiss()
                }
            }
            deckNameCheckResult.observe {
                renameDeckEditText.error = when (it) {
                    Ok -> null
                    Empty -> getString(R.string.error_message_empty_name)
                    Occupied -> getString(R.string.error_message_occupied_name)
                }
            }
            exercisePreference.observe { exercisePreference: ExercisePreference ->
                val exercisePreferenceName = when {
                    exercisePreference.isDefault() -> getString(R.string.default_name)
                    exercisePreference.isIndividual() -> getString(R.string.individual_name)
                    else -> "'${exercisePreference.name}'"
                }
                presetNameTextView.text = exercisePreferenceName
            }
            isSaveExercisePreferenceButtonEnabled.observe { isEnabled ->
                saveExercisePreferencesButton.visibility = if (isEnabled) VISIBLE else GONE
            }
            availableExercisePreferences.observe(exercisePreferenceAdapter::submitList)
            isNamePresetDialogVisible.observe { isVisible ->
                namePresetDialog.run {
                    if (isVisible) show() else dismiss()
                }
            }
            namePresetInputCheckResult.observe {
                namePresetEditText.error = when (it) {
                    Ok -> null
                    Empty -> getString(R.string.error_message_empty_name)
                    Occupied -> getString(R.string.error_message_occupied_name)
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
                    Off -> getString(R.string.test_method_label_off)
                    Manual -> getString(R.string.test_method_label_manual)
                    Quiz -> getString(R.string.test_method_label_quiz)
                    Entry -> getString(R.string.test_method_label_entry)
                }

                val testMethods = TestMethod.values().map {
                    TestMethodItem(
                        testMethod = it,
                        text = when (it) {
                            Off -> getString(R.string.test_method_label_off)
                            Manual -> getString(R.string.test_method_label_manual)
                            Quiz -> getString(R.string.test_method_label_quiz)
                            Entry -> getString(R.string.test_method_label_entry)
                        },
                        isSelected = it === selectedTestMethod
                    )
                }
                testMethodAdapter.items = testMethods
            }
            intervalScheme.observe {
                selectedIntervalsTextView.text = when {
                    it == null -> getString(R.string.off)
                    it.id == 0L -> getString(R.string.default_name)
                    it.name.isEmpty() -> getString(R.string.individual_name)
                    else -> "'${it.name}'"
                }
            }
            pronunciationIdAndName.observe {
                selectedPronunciationTextView.text = when {
                    it.id == 0L -> getString(R.string.default_name)
                    it.name.isEmpty() -> getString(R.string.individual_name)
                    else -> "'${it.name}'"
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

    private fun executeCommand(command: DeckSettingsCommand) {
        when (command) {
            is SetRenameDeckDialogText -> {
                renameDeckEditText.setText(command.text)
                renameDeckEditText.selectAll()
            }
            is SetNamePresetDialogText -> {
                namePresetEditText.setText(command.text)
                namePresetEditText.selectAll()
            }
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.run {
            getBundle(STATE_KEY_RENAME_DECK_DIALOG)
                ?.let(renameDeckDialog::onRestoreInstanceState)

            getBundle(STATE_KEY_PRESET_NAME_INPUT_DIALOG)
                ?.let(namePresetDialog::onRestoreInstanceState)

            getBundle(STATE_KEY_CHOOSE_TEST_METHOD_DIALOG)
                ?.let(chooseTestMethodDialog::onRestoreInstanceState)

            getBundle(STATE_KEY_CHOOSE_CARD_REVERSE_DIALOG)
                ?.let(chooseCardReverseDialog::onRestoreInstanceState)
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
        if (::namePresetDialog.isInitialized) {
            outState.putBundle(
                STATE_KEY_PRESET_NAME_INPUT_DIALOG,
                namePresetDialog.onSaveInstanceState()
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

    override fun onDestroy() {
        super.onDestroy()
        if (isRemoving) {
            controller.onFragmentRemoving()
        }
    }

    companion object {
        const val STATE_KEY_RENAME_DECK_DIALOG = "renameDeckDialog"
        const val STATE_KEY_PRESET_NAME_INPUT_DIALOG = "presetNameInputDialog"
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