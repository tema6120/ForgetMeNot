package com.odnovolov.forgetmenot.screen.decksettings

import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.EditText
import android.widget.PopupWindow
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.common.base.BaseFragment
import com.odnovolov.forgetmenot.common.entity.NameCheckResult.*
import com.odnovolov.forgetmenot.common.entity.TestMethod
import com.odnovolov.forgetmenot.common.customview.InputDialogCreator
import com.odnovolov.forgetmenot.common.customview.PresetPopupCreator
import com.odnovolov.forgetmenot.common.customview.PresetPopupCreator.PresetRecyclerAdapter
import com.odnovolov.forgetmenot.common.customview.ChoiceDialogCreator
import com.odnovolov.forgetmenot.common.customview.ChoiceDialogCreator.Item
import com.odnovolov.forgetmenot.common.customview.ChoiceDialogCreator.ItemAdapter
import com.odnovolov.forgetmenot.common.customview.ChoiceDialogCreator.ItemForm.AsRadioButton
import com.odnovolov.forgetmenot.common.entity.TestMethod.*
import com.odnovolov.forgetmenot.screen.decksettings.DeckSettingsEvent.*
import com.odnovolov.forgetmenot.screen.decksettings.DeckSettingsOrder.*
import kotlinx.android.synthetic.main.fragment_deck_settings.*
import leakcanary.LeakSentry

class DeckSettingsFragment : BaseFragment() {

    private val viewModel = DeckSettingsViewModel()
    private val controller = DeckSettingsController()
    private lateinit var chooseExercisePreferencePopup: PopupWindow
    private lateinit var exercisePreferenceAdapter: PresetRecyclerAdapter
    private lateinit var presetNameInputDialog: Dialog
    private lateinit var presetNameInput: EditText
    private lateinit var chooseTestMethodDialog: Dialog
    private lateinit var testMethodAdapter: ItemAdapter<TestMethodItem>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initChooseExercisePreferencePopup()
        initPresetNameInputDialog()
        initChooseTestMethodDialog()
        return inflater.inflate(R.layout.fragment_deck_settings, container, false)
    }

    private fun initChooseExercisePreferencePopup() {
        chooseExercisePreferencePopup = PresetPopupCreator.create(
            context = requireContext(),
            setPresetButtonClickListener = { id: Long ->
                controller.dispatch(SetExercisePreferenceButtonClicked(id))
            },
            renamePresetButtonClickListener = { id: Long ->
                controller.dispatch(RenameExercisePreferenceButtonClicked(id))
            },
            deletePresetButtonClickListener = { id: Long ->
                controller.dispatch(DeleteExercisePreferenceButtonClicked(id))
            },
            addButtonClickListener = {
                controller.dispatch(AddNewExercisePreferenceButtonClicked)
            },
            takeAdapter = { exercisePreferenceAdapter = it }
        )
    }

    private fun initPresetNameInputDialog() {
        presetNameInputDialog = InputDialogCreator.create(
            context = requireContext(),
            title = getString(R.string.title_exercise_preference_name_input_dialog),
            takeEditText = { presetNameInput = it },
            onTextChanged = { controller.dispatch(DialogTextChanged(it.toString())) },
            onPositiveClick = { controller.dispatch(PositiveDialogButtonClicked) },
            onNegativeClick = { controller.dispatch(NegativeDialogButtonClicked) }
        )
    }

    private fun initChooseTestMethodDialog() {
        chooseTestMethodDialog = ChoiceDialogCreator.create<TestMethodItem>(
            context = requireContext(),
            title = getString(R.string.title_choose_test_method_dialog),
            itemForm = AsRadioButton,
            onItemClick = {
                val chosenTestMethod = it.testMethod
                controller.dispatch(TestMethodWasChosen(chosenTestMethod))
                chooseTestMethodDialog.dismiss()
            },
            takeAdapter = { testMethodAdapter = it }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        observeViewModel()
        controller.orders.forEach(execute = ::executeOrder)
    }

    private fun setupView() {
        renameDeckButton.setOnClickListener {
            controller.dispatch(RenameDeckButtonClicked)
        }
        saveExercisePreferencesButton.setOnClickListener {
            controller.dispatch(SaveExercisePreferenceButtonClicked)
        }
        presetNameTextView.setOnClickListener {
            showChooseExercisePreferencePopup()
        }
        randomButton.setOnClickListener {
            controller.dispatch(RandomOrderSwitchToggled)
        }
        testMethodButton.setOnClickListener {
            showChooseTestMethodDialog()
        }
        intervalsButton.setOnClickListener {
            controller.dispatch(IntervalsButtonClicked)
        }
        pronunciationButton.setOnClickListener {
            controller.dispatch(PronunciationButtonClicked)
        }
        displayQuestionButton.setOnClickListener {
            controller.dispatch(DisplayQuestionSwitchToggled)
        }
    }

    private fun showChooseTestMethodDialog() {
        chooseTestMethodDialog.show()
    }

    private fun showChooseExercisePreferencePopup() {
        val location = IntArray(2)
        presetNameTextView.getLocationOnScreen(location)
        val x = location[0] + presetNameTextView.width - chooseExercisePreferencePopup.width
        val y = location[1]
        chooseExercisePreferencePopup.showAtLocation(
            presetNameTextView.rootView,
            Gravity.NO_GRAVITY, x, y
        )
    }

    private fun observeViewModel() {
        with(viewModel) {
            deckName.observe(onChange = deckNameTextView::setText)
            exercisePreferenceIdAndName.observe {
                val exercisePreferenceName = when {
                    it.id == 0L -> getString(R.string.default_name)
                    it.name.isEmpty() -> getString(R.string.individual_name)
                    else -> "'${it.name}'"
                }
                presetNameTextView.text = exercisePreferenceName
            }
            isSaveExercisePreferenceButtonEnabled.observe { isEnabled ->
                saveExercisePreferencesButton.visibility = if (isEnabled) VISIBLE else GONE
            }
            availableExercisePreferences.observe(onChange = exercisePreferenceAdapter::submitList)
            isDialogVisible.observe { isDialogVisible ->
                if (isDialogVisible) {
                    presetNameInputDialog.show()
                } else {
                    presetNameInputDialog.dismiss()
                }
            }
            dialogInputCheckResult.observe {
                presetNameInput.error = when (it) {
                    OK -> null
                    EMPTY -> getString(R.string.error_message_empty_name)
                    OCCUPIED -> getString(R.string.error_message_occupied_name)
                }
            }
            randomOrder.observe(
                onChange = randomOrderSwitch::setChecked,
                afterFirst = {
                    randomOrderSwitch.jumpDrawablesToCurrentState()
                    randomOrderSwitch.visibility = VISIBLE
                })
            selectedTestMethod.observe { selectedTestMethod ->
                selectedTestMethodTextView.text = when (selectedTestMethod) {
                    Off -> getString(R.string.test_method_label_off)
                    Manual -> getString(R.string.test_method_label_manual)
                    Quiz -> getString(R.string.test_method_label_quiz)
                }

                val testMethods = TestMethod.values().map {
                    TestMethodItem(
                        testMethod = it,
                        text = when (it) {
                            Off -> getString(R.string.test_method_label_off)
                            Manual -> getString(R.string.test_method_label_manual)
                            Quiz -> getString(R.string.test_method_label_quiz)
                        },
                        isSelected = it === selectedTestMethod
                    )
                }
                testMethodAdapter.submitList(testMethods)
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
            isQuestionDisplayed.observe(
                onChange = displayQuestionSwitch::setChecked,
                afterFirst = {
                    displayQuestionSwitch.jumpDrawablesToCurrentState()
                    displayQuestionSwitch.visibility = VISIBLE
                })
        }
    }

    private fun executeOrder(order: DeckSettingsOrder) {
        when (order) {
            ShowRenameDeckDialog -> {
                Toast.makeText(requireContext(), "Not implemented", Toast.LENGTH_SHORT)
                    .show()
            }
            is SetDialogText -> {
                presetNameInput.setText(order.text)
                presetNameInput.selectAll()
            }
            NavigateToIntervals -> {
                findNavController().navigate(R.id.action_deck_settings_screen_to_intervals_screen)
            }
            NavigateToPronunciation -> {
                findNavController()
                    .navigate(R.id.action_deck_settings_screen_to_pronunciation_screen)
            }
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        val presetNameInputDialogState =
            savedInstanceState?.getBundle(STATE_KEY_PRESET_NAME_INPUT_DIALOG)
        if (presetNameInputDialogState != null) {
            presetNameInputDialog.onRestoreInstanceState(presetNameInputDialogState)
        }

        val chooseTestMethodDialogState =
            savedInstanceState?.getBundle(STATE_KEY_CHOOSE_TEST_METHOD_DIALOG)
        if (chooseTestMethodDialogState != null) {
            chooseTestMethodDialog.onRestoreInstanceState(chooseTestMethodDialogState)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (::presetNameInputDialog.isInitialized) {
            outState.putBundle(
                STATE_KEY_PRESET_NAME_INPUT_DIALOG,
                presetNameInputDialog.onSaveInstanceState()
            )
        }
        if (::chooseTestMethodDialog.isInitialized) {
            outState.putBundle(
                STATE_KEY_CHOOSE_TEST_METHOD_DIALOG,
                chooseTestMethodDialog.onSaveInstanceState()
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        controller.dispose()
        LeakSentry.refWatcher.watch(this)
    }

    companion object {
        const val STATE_KEY_PRESET_NAME_INPUT_DIALOG = "presetNameInputDialog"
        const val STATE_KEY_CHOOSE_TEST_METHOD_DIALOG = "chooseTestMethodDialog"
    }
}

data class TestMethodItem(
    val testMethod: TestMethod,
    override val text: String,
    override val isSelected: Boolean
) : Item