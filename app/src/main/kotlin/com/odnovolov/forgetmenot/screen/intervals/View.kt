package com.odnovolov.forgetmenot.screen.intervals

import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.EditText
import android.widget.PopupWindow
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.common.base.BaseFragment
import com.odnovolov.forgetmenot.common.customview.InputDialogCreator
import com.odnovolov.forgetmenot.common.customview.PresetPopupCreator
import com.odnovolov.forgetmenot.common.customview.PresetPopupCreator.PresetRecyclerAdapter
import com.odnovolov.forgetmenot.common.database.Interval
import com.odnovolov.forgetmenot.common.database.IntervalScheme
import com.odnovolov.forgetmenot.common.entity.NameCheckResult.*
import com.odnovolov.forgetmenot.screen.intervals.IntervalAdapter.ViewHolder
import com.odnovolov.forgetmenot.screen.intervals.IntervalsEvent.*
import com.odnovolov.forgetmenot.screen.intervals.IntervalsOrder.SetDialogStatus
import com.odnovolov.forgetmenot.screen.intervals.IntervalsOrder.ShowModifyIntervalDialog
import com.odnovolov.forgetmenot.screen.intervals.modifyinterval.ModifyIntervalFragment
import kotlinx.android.synthetic.main.fragment_intervals.*
import kotlinx.android.synthetic.main.item_interval.view.*

class IntervalsFragment : BaseFragment() {
    private val controller = IntervalsController()
    private val viewModel = IntervalsViewModel()
    private val adapter = IntervalAdapter(controller)
    private lateinit var chooseIntervalSchemePopup: PopupWindow
    private lateinit var intervalSchemeRecyclerAdapter: PresetRecyclerAdapter
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
                controller.dispatch(SetIntervalSchemeButtonClicked(id))
            },
            renamePresetButtonClickListener = { id: Long ->
                controller.dispatch(RenameIntervalSchemeButtonClicked(id))
            },
            deletePresetButtonClickListener = { id: Long ->
                controller.dispatch(DeleteIntervalSchemeButtonClicked(id))
            },
            addButtonClickListener = {
                controller.dispatch(AddNewIntervalSchemeButtonClicked)
            },
            takeAdapter = { intervalSchemeRecyclerAdapter = it }
        )
    }

    private fun initPresetNameInputDialog() {
        presetNameInputDialog = InputDialogCreator.create(
            context = requireContext(),
            title = getString(R.string.title_interval_scheme_name_input_dialog),
            takeEditText = { presetNameEditText = it },
            onTextChanged = { controller.dispatch(DialogTextChanged(it)) },
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
        saveIntervalSchemeButton.setOnClickListener {
            controller.dispatch(SaveIntervalSchemeButtonClicked)
        }
        intervalSchemeNameTextView.setOnClickListener {
            showChooseIntervalSchemePopup()
        }
        intervalsRecyclerView.adapter = adapter
        addIntervalButton.setOnClickListener {
            controller.dispatch(AddIntervalButtonClicked)
        }
        removeIntervalButton.setOnClickListener {
            controller.dispatch(RemoveIntervalButtonClicked)
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
            currentIntervalScheme.observe { intervalScheme: IntervalScheme? ->
                intervalSchemeNameTextView.text = when {
                    intervalScheme == null -> getString(R.string.off)
                    intervalScheme.id == 0L -> getString(R.string.default_name)
                    intervalScheme.name.isEmpty() -> getString(R.string.individual_name)
                    else -> "'${intervalScheme.name}'"
                }
                intervalsEditorGroup.visibility = if (intervalScheme == null) INVISIBLE else VISIBLE
            }
            isSaveIntervalSchemeButtonEnabled.observe { isEnabled ->
                saveIntervalSchemeButton.visibility = if (isEnabled) VISIBLE else GONE
            }
            availableIntervalSchemes.observe(onChange = intervalSchemeRecyclerAdapter::submitList)
            isNamePresetDialogVisible.observe { isVisible ->
                presetNameInputDialog.run { if (isVisible) show() else dismiss() }
            }
            dialogInputCheckResult.observe {
                presetNameEditText.error = when (it) {
                    OK -> null
                    EMPTY -> getString(R.string.error_message_empty_name)
                    OCCUPIED -> getString(R.string.error_message_occupied_name)
                }
            }
            intervals.observe(onChange = adapter::submitList)
            isRemoveIntervalButtonEnabled.observe { isEnabled: Boolean ->
                removeIntervalButton.run { if (isEnabled) show() else hide() }
            }
        }
    }

    private fun executeOrder(order: IntervalsOrder) {
        when (order) {
            ShowModifyIntervalDialog -> {
                ModifyIntervalFragment().show(childFragmentManager, "ModifyIntervalFragment")
            }

            is SetDialogStatus -> {
                presetNameEditText.setText(order.text)
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
        controller.dispose()
    }

    companion object {
        const val STATE_KEY_DIALOG = "intervalSchemeNameInputDialog"
    }
}

class IntervalAdapter(private val controller: IntervalsController) :
    ListAdapter<Interval, ViewHolder>(DiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_interval, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.itemView) {
            val interval: Interval = getItem(position)
            val backgroundRes = when (interval.targetLevelOfKnowledge) {
                1 -> R.drawable.background_level_of_knowledge_poor
                2 -> R.drawable.background_level_of_knowledge_acceptable
                3 -> R.drawable.background_level_of_knowledge_satisfactory
                4 -> R.drawable.background_level_of_knowledge_good
                5 -> R.drawable.background_level_of_knowledge_very_good
                else -> R.drawable.background_level_of_knowledge_excellent
            }
            levelOfKnowledgeTextView.setBackgroundResource(backgroundRes)
            levelOfKnowledgeTextView.text = interval.targetLevelOfKnowledge.toString()

            intervalTextView.text = interval.value
            modifyIntervalButton.setOnClickListener {
                controller.dispatch(
                    ModifyIntervalButtonClicked(interval.targetLevelOfKnowledge)
                )
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    class DiffCallback : DiffUtil.ItemCallback<Interval>() {
        override fun areItemsTheSame(oldItem: Interval, newItem: Interval): Boolean {
            return oldItem.targetLevelOfKnowledge == newItem.targetLevelOfKnowledge
        }

        override fun areContentsTheSame(oldItem: Interval, newItem: Interval): Boolean {
            return oldItem.targetLevelOfKnowledge == newItem.targetLevelOfKnowledge
                    && oldItem.value == newItem.value
        }
    }
}