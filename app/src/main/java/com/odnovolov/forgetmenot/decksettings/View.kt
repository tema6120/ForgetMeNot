package com.odnovolov.forgetmenot.decksettings

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
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.common.BaseFragment
import com.odnovolov.forgetmenot.common.NameCheckResult.*
import com.odnovolov.forgetmenot.common.PresetPopupCreator
import com.odnovolov.forgetmenot.common.PresetPopupCreator.PresetRecyclerAdapter
import com.odnovolov.forgetmenot.common.createInputDialog
import com.odnovolov.forgetmenot.decksettings.DeckSettingsEvent.*
import com.odnovolov.forgetmenot.decksettings.DeckSettingsOrder.NavigateToPronunciation
import com.odnovolov.forgetmenot.decksettings.DeckSettingsOrder.ShowRenameDeckDialog
import kotlinx.android.synthetic.main.fragment_deck_settings.*
import leakcanary.LeakSentry

class DeckSettingsFragment : BaseFragment() {

    private val viewModel = DeckSettingsViewModel()
    private val controller = DeckSettingsController()
    private lateinit var chooseExercisePreferencePopup: PopupWindow
    private lateinit var exercisePreferenceAdapter: PresetRecyclerAdapter
    private lateinit var presetNameInputDialog: AlertDialog
    private lateinit var presetNameInput: EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initChooseExercisePreferencePopup()
        initDialog()
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
            getAdapter = { exercisePreferenceAdapter = it }
        )
    }

    private fun initDialog() {
        presetNameInputDialog = createInputDialog(
            title = getString(R.string.title_exercise_preference_name_input_dialog),
            takeEditText = { presetNameInput = it },
            onTextChanged = { controller.dispatch(DialogTextChanged(it.toString())) },
            onPositiveClick = { controller.dispatch(PositiveDialogButtonClicked) },
            onNegativeClick = { controller.dispatch(NegativeDialogButtonClicked) }
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
        pronunciationButton.setOnClickListener {
            controller.dispatch(PronunciationButtonClicked)
        }
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
            pronunciationIdAndName.observe {
                selectedPronunciationTextView.text = when {
                    it.id == 0L -> getString(R.string.default_name)
                    it.name.isEmpty() -> getString(R.string.individual_name)
                    else -> "'${it.name}'"
                }
            }
        }
    }

    private fun executeOrder(order: DeckSettingsOrder) {
        when (order) {
            ShowRenameDeckDialog -> {
                Toast.makeText(requireContext(), "Not implemented", Toast.LENGTH_SHORT)
                    .show()
            }
            is NavigateToPronunciation -> {
                findNavController()
                    .navigate(R.id.action_deck_settings_screen_to_pronunciation_screen)
            }
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        val dialogState = savedInstanceState?.getBundle(STATE_KEY_DIALOG)
        if (dialogState != null) {
            presetNameInputDialog.onRestoreInstanceState(dialogState)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBundle(STATE_KEY_DIALOG, presetNameInputDialog.onSaveInstanceState())
    }

    override fun onDestroy() {
        super.onDestroy()
        controller.dispose()
        LeakSentry.refWatcher.watch(this)
    }

    companion object {
        const val STATE_KEY_DIALOG = "presetNameInputDialog"
    }
}