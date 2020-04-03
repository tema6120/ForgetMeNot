package com.odnovolov.forgetmenot.presentation.common.preset

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
import android.widget.EditText
import android.widget.PopupWindow
import androidx.appcompat.app.AlertDialog
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult.*
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.dp
import com.odnovolov.forgetmenot.presentation.common.observeText
import com.odnovolov.forgetmenot.presentation.common.preset.SkeletalPresetController.Command.ShowDialogWithText
import com.odnovolov.forgetmenot.presentation.common.showSoftInput
import kotlinx.android.synthetic.main.dialog_input.view.*
import kotlinx.android.synthetic.main.fragment_preset.*
import kotlinx.android.synthetic.main.popup_preset.view.*

class PresetFragment : BaseFragment() {
    lateinit var viewModel: SkeletalPresetViewModel // it's initialized from parent fragment
    lateinit var controller: SkeletalPresetController // it's initialized from parent fragment
    private lateinit var popup: PopupWindow
    private lateinit var presetAdapter: PresetAdapter
    private lateinit var nameInputDialog: AlertDialog
    private lateinit var nameEditText: EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initPopup()
        initNameInputDialog()
        return inflater.inflate(R.layout.fragment_preset, container, false)
    }

    private fun initPopup() {
        popup = PopupWindow(context).apply {
            width = 256.dp
            height = WindowManager.LayoutParams.WRAP_CONTENT
            setBackgroundDrawable(ColorDrawable(Color.WHITE))
            elevation = 20f
            isOutsideTouchable = true
            isFocusable = true
            softInputMode = SOFT_INPUT_STATE_ALWAYS_HIDDEN
            contentView = View.inflate(context, R.layout.popup_preset, null)
            presetAdapter = PresetAdapter(
                onSetPresetButtonClick = { id: Long? ->
                    controller.onSetPresetButtonClicked(id)
                    dismiss()
                },
                onRenamePresetButtonClick = { id: Long ->
                    controller.onRenamePresetButtonClicked(id)
                },
                onDeletePresetButtonClick = { id: Long ->
                    controller.onDeletePresetButtonClicked(id)
                }
            )
            contentView.presetRecyclerView.adapter = presetAdapter
            contentView.addPresetButton.setOnClickListener {
                controller.onAddNewPresetButtonClicked()
            }
        }
    }

    private fun initNameInputDialog() {
        val contentView = View.inflate(context, R.layout.dialog_input, null)
        nameEditText = contentView.dialogInput
        nameEditText.observeText { controller.onPresetNameInputChanged(it.toString()) }
        nameInputDialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.title_preset_name_input_dialog)
            .setView(contentView)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                controller.onPresetNamePositiveDialogButtonClicked()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
        nameInputDialog.setOnShowListener { nameEditText.showSoftInput() }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        observeViewModel()
        controller.commands.observe(::executeCommand)
    }

    private fun setupView() {
        savePresetButton.setOnClickListener { controller.onSavePresetButtonClicked() }
        selectPresetButton.setOnClickListener { showPopup() }
    }

    private fun showPopup() {
        val location = IntArray(2)
        selectPresetButton.getLocationOnScreen(location)
        val x = location[0] + selectPresetButton.width - popup.width
        val y = location[1]
        popup.showAtLocation(requireParentFragment().view, Gravity.NO_GRAVITY, x, y)
    }

    private fun observeViewModel() {
        with(viewModel) {
            currentPreset.observe { preset: Preset ->
                selectPresetButton.text = preset.toString(requireContext())
                savePresetButton.visibility = if (preset.isIndividual()) VISIBLE else GONE
            }
            availablePresets.observe(presetAdapter::submitList)
            presetInputCheckResult.observe { nameCheckResult: NameCheckResult ->
                nameEditText.error = when (nameCheckResult) {
                    Ok -> null
                    Empty -> getString(R.string.error_message_empty_name)
                    Occupied -> getString(R.string.error_message_occupied_name)
                }
                if (nameInputDialog.isShowing) {
                    nameInputDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .isEnabled = nameCheckResult == Ok
                }
            }
        }
    }

    private fun executeCommand(command: SkeletalPresetController.Command) {
        when (command) {
            is ShowDialogWithText -> {
                nameInputDialog.show()
                nameEditText.setText(command.text)
                nameEditText.selectAll()
            }
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.getBundle(STATE_KEY_NAME_INPUT_DIALOG)
            ?.let(nameInputDialog::onRestoreInstanceState)
    }

    override fun onPause() {
        super.onPause()
        controller.onFragmentPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBundle(STATE_KEY_NAME_INPUT_DIALOG, nameInputDialog.onSaveInstanceState())
    }

    companion object {
        const val STATE_KEY_NAME_INPUT_DIALOG = "STATE_KEY_NAME_INPUT_DIALOG"
    }
}