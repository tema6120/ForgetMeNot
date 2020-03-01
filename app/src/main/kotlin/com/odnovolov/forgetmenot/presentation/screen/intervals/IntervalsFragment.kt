package com.odnovolov.forgetmenot.presentation.screen.intervals

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
import com.odnovolov.forgetmenot.presentation.common.customview.InputDialogCreator
import com.odnovolov.forgetmenot.presentation.common.customview.PresetPopupCreator
import com.odnovolov.forgetmenot.presentation.common.customview.PresetPopupCreator.PresetAdapter
import com.odnovolov.forgetmenot.domain.entity.IntervalScheme
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult.*
import com.odnovolov.forgetmenot.domain.isDefault
import com.odnovolov.forgetmenot.domain.isIndividual
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.screen.intervals.IntervalsCommand.SetNamePresetDialogText
import com.odnovolov.forgetmenot.presentation.screen.intervals.IntervalsCommand.ShowModifyIntervalDialog
import com.odnovolov.forgetmenot.presentation.screen.intervals.modifyinterval.ModifyIntervalFragment
import kotlinx.android.synthetic.main.fragment_intervals.*
import org.koin.android.ext.android.getKoin
import org.koin.androidx.viewmodel.scope.viewModel


class IntervalsFragment : BaseFragment() {
    private val koinScope = getKoin().getOrCreateScope<IntervalsViewModel>(INTERVALS_SCOPE_ID)
    private val viewModel: IntervalsViewModel by koinScope.viewModel(this)
    private val controller: IntervalsController by koinScope.inject()
    private val adapter: IntervalAdapter by lazy { IntervalAdapter(controller) }
    private lateinit var chooseIntervalSchemePopup: PopupWindow
    private lateinit var intervalSchemeRecyclerAdapter: PresetAdapter
    private lateinit var presetNameInputDialog: Dialog
    private lateinit var presetNameEditText: EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initChooseIntervalSchemePopup()
        initPresetNameInputDialog()
        return inflater.inflate(R.layout.fragment_intervals, container, false)
    }

    private fun initChooseIntervalSchemePopup() {
        chooseIntervalSchemePopup = PresetPopupCreator.create(
            context = requireContext(),
            setPresetButtonClickListener = { id: Long? ->
                controller.onSetIntervalSchemeButtonClicked(id)
            },
            renamePresetButtonClickListener = { id: Long ->
                controller.onRenameIntervalSchemeButtonClicked(id)
            },
            deletePresetButtonClickListener = { id: Long ->
                controller.onDeleteIntervalSchemeButtonClicked(id)
            },
            addButtonClickListener = {
                controller.onAddNewIntervalSchemeButtonClicked()
            },
            takeAdapter = { intervalSchemeRecyclerAdapter = it }
        )
    }

    private fun initPresetNameInputDialog() {
        presetNameInputDialog = InputDialogCreator.create(
            context = requireContext(),
            title = getString(R.string.title_interval_scheme_name_input_dialog),
            takeEditText = { presetNameEditText = it },
            onTextChanged = { controller.onDialogTextChanged(it) },
            onPositiveClick = { controller.onNamePresetPositiveDialogButtonClicked() },
            onNegativeClick = { controller.onNamePresetNegativeDialogButtonClicked() }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        observeViewModel()
        controller.commands.observe(::executeCommand)
    }

    private fun setupView() {
        saveIntervalSchemeButton.setOnClickListener {
            controller.onSaveIntervalSchemeButtonClicked()
        }
        intervalSchemeNameTextView.setOnClickListener {
            showChooseIntervalSchemePopup()
        }
        intervalsRecyclerView.adapter = adapter
        addIntervalButton.setOnClickListener {
            controller.onAddIntervalButtonClicked()
        }
        removeIntervalButton.setOnClickListener {
            controller.onRemoveIntervalButtonClicked()
        }
    }

    private fun showChooseIntervalSchemePopup() {
        val location = IntArray(2)
        intervalSchemeNameTextView.getLocationOnScreen(location)
        val x = location[0] + intervalSchemeNameTextView.width - chooseIntervalSchemePopup.width
        val y = location[1]
        chooseIntervalSchemePopup.showAtLocation(rootView, Gravity.NO_GRAVITY, x, y)
    }

    private fun observeViewModel() {
        with(viewModel) {
            intervalScheme.observe { intervalScheme: IntervalScheme? ->
                intervalSchemeNameTextView.text = when {
                    intervalScheme == null -> getString(R.string.off)
                    intervalScheme.isDefault() -> getString(R.string.default_name)
                    intervalScheme.isIndividual() -> getString(R.string.individual_name)
                    else -> "'${intervalScheme.name}'"
                }
                intervalsEditorGroup.visibility = if (intervalScheme == null) INVISIBLE else VISIBLE
            }
            isSaveIntervalSchemeButtonEnabled.observe { isEnabled ->
                saveIntervalSchemeButton.visibility = if (isEnabled) VISIBLE else GONE
            }
            availableIntervalSchemes.observe(intervalSchemeRecyclerAdapter::submitList)
            isNamePresetDialogVisible.observe { isVisible ->
                presetNameInputDialog.run { if (isVisible) show() else dismiss() }
            }
            namePresetInputCheckResult.observe {
                presetNameEditText.error = when (it) {
                    Ok -> null
                    Empty -> getString(R.string.error_message_empty_name)
                    Occupied -> getString(R.string.error_message_occupied_name)
                }
            }
            intervals.observe(adapter::submitList)
            isRemoveIntervalButtonEnabled.observe { isEnabled: Boolean ->
                removeIntervalButton.isEnabled = isEnabled
            }
        }
    }

    private fun executeCommand(command: IntervalsCommand) {
        when (command) {
            ShowModifyIntervalDialog -> {
                ModifyIntervalFragment().show(childFragmentManager, MODIFY_INTERVAL_FRAGMENT_TAG)
            }

            is SetNamePresetDialogText -> {
                presetNameEditText.setText(command.text)
                presetNameEditText.selectAll()
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

    override fun onDestroyView() {
        super.onDestroyView()
        intervalsRecyclerView.adapter = null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isRemoving) {
            controller.onFragmentRemoving()
        }
    }

    companion object {
        const val STATE_KEY_DIALOG = "intervalSchemeNameInputDialog"
        const val MODIFY_INTERVAL_FRAGMENT_TAG = "ModifyIntervalFragment"
    }
}