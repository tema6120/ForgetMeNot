package com.odnovolov.forgetmenot.presentation.screen.repetitionsettings

import android.app.Dialog
import android.os.Bundle
import android.view.*
import android.view.View.*
import android.widget.CheckBox
import android.widget.EditText
import android.widget.PopupWindow
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.appyvet.materialrangebar.RangeBar
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult.*
import com.odnovolov.forgetmenot.domain.entity.RepetitionSetting
import com.odnovolov.forgetmenot.domain.interactor.repetition.RepetitionSettings
import com.odnovolov.forgetmenot.domain.isDefault
import com.odnovolov.forgetmenot.domain.isIndividual
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.customview.InputDialogCreator
import com.odnovolov.forgetmenot.presentation.common.customview.PresetPopupCreator
import com.odnovolov.forgetmenot.presentation.common.preset.PresetAdapter
import com.odnovolov.forgetmenot.presentation.common.entity.DisplayedInterval
import com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.RepetitionSettingsController.Command.SetNamePresetDialogText
import com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.RepetitionSettingsController.Command.ShowNoCardIsReadyForRepetitionMessage
import kotlinx.android.synthetic.main.fragment_repetition_settings.*
import org.koin.android.ext.android.getKoin
import org.koin.androidx.viewmodel.scope.viewModel
import java.util.*
import kotlin.collections.ArrayList

class RepetitionSettingsFragment : BaseFragment() {
    private val koinScope =
        getKoin().getOrCreateScope<RepetitionSettings>(REPETITION_SETTINGS_SCOPE_ID)
    private val viewModel: RepetitionSettingsViewModel by koinScope.viewModel(this)
    private val controller: RepetitionSettingsController by koinScope.inject()
    private lateinit var presetPopup: PopupWindow
    private lateinit var presetAdapter: PresetAdapter
    private lateinit var namePresetDialog: Dialog
    private lateinit var namePresetEditText: EditText
    private var isLevelOfKnowledgeRangeListenerEnabled = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        initPresetPopup()
        initNamePresetDialog()
        return inflater.inflate(R.layout.fragment_repetition_settings, container, false)
    }

    private fun initPresetPopup() {
        presetPopup = PresetPopupCreator.create(
            context = requireContext(),
            setPresetButtonClickListener = { repetitionSettingsId: Long? ->
                controller.onSetRepetitionSettingsClicked(repetitionSettingsId!!)
            },
            renamePresetButtonClickListener = { repetitionSettingsId: Long ->
                controller.onRenameRepetitionSettingsClicked(repetitionSettingsId)
            },
            deletePresetButtonClickListener = { repetitionSettingsId: Long ->
                controller.onDeleteRepetitionSettingsClicked(repetitionSettingsId)
            },
            addButtonClickListener = {
                controller.onAddNewRepetitionSettingsClicked()
            },
            takeAdapter = { presetAdapter = it }
        )
    }

    private fun initNamePresetDialog() {
        namePresetDialog = InputDialogCreator.create(
            context = requireContext(),
            title = getString(R.string.title_exercise_preference_name_input_dialog),
            takeEditText = { namePresetEditText = it },
            onTextChanged = { controller.onDialogTextChanged(it) },
            onPositiveClick = { controller.onNamePresetPositiveDialogButtonClicked() },
            onNegativeClick = { controller.onNamePresetNegativeDialogButtonClicked() }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMatchingCardsLabel()
        setupPresetPanel()
        setupFilterGroups()
        setupLevelOfKnowledgeRangeBar()
        setupLastAnswerFilter()
        setupNumberOfLaps()
        controller.commands.observe(::executeCommand)
    }

    private fun setupMatchingCardsLabel() {
        viewModel.matchingCardsNumber.observe { matchingCardsNumber: Int ->
            matchingCardsNumberTextView.text = matchingCardsNumber.toString()
            matchingCardsLabelTextView.text = resources.getQuantityString(
                R.plurals.matching_cards_number_label,
                matchingCardsNumber
            )
        }
    }

    private fun setupPresetPanel() {
        with(viewModel) {
            repetitionSetting.observe { repetitionSetting: RepetitionSetting ->
                presetButton.text = when {
                    repetitionSetting.isDefault() -> getString(R.string.default_name)
                    repetitionSetting.isIndividual() -> getString(R.string.individual_name)
                    else -> "'${repetitionSetting.name}'"
                }
            }
            isSavePresetButtonEnabled.observe { isEnabled: Boolean ->
                savePresetButton.visibility = if (isEnabled) VISIBLE else GONE
            }
            availablePresets.observe(presetAdapter::submitList)
            isNamePresetDialogVisible.observe { isVisible: Boolean ->
                namePresetDialog.run { if (isVisible) show() else dismiss() }
            }
            namePresetInputCheckResult.observe { nameCheckResult: NameCheckResult ->
                namePresetEditText.error = when (nameCheckResult) {
                    Ok -> null
                    Empty -> getString(R.string.error_message_empty_name)
                    Occupied -> getString(R.string.error_message_occupied_name)
                }
            }
        }
        savePresetButton.setOnClickListener { controller.onSavePresetButtonClicked() }
        presetButton.setOnClickListener { showPresetPopup() }
    }

    private fun showPresetPopup() {
        val location = IntArray(2)
        presetButton.getLocationOnScreen(location)
        val x = location[0] + presetButton.width - presetPopup.width
        val y = location[1]
        presetPopup.showAtLocation(rootView, Gravity.NO_GRAVITY, x, y)
    }

    private fun setupFilterGroups() {
        with(viewModel) {
            fun updateCheckBox(checkBox: CheckBox, isChecked: Boolean) {
                with(checkBox) {
                    setChecked(isChecked)
                    if (visibility == INVISIBLE) {
                        jumpDrawablesToCurrentState()
                        visibility = VISIBLE
                    }
                }
            }
            isAvailableForExerciseGroupChecked.observe { isChecked: Boolean ->
                updateCheckBox(availableForExerciseGroupCheckBox, isChecked)
            }
            isAwaitingGroupChecked.observe { isChecked: Boolean ->
                updateCheckBox(awaitingGroupCheckBox, isChecked)
            }
            isLearnedGroupChecked.observe { isChecked: Boolean ->
                updateCheckBox(learnedGroupCheckBox, isChecked)
            }
        }
        availableForExerciseGroupButton.setOnClickListener {
            controller.onAvailableForExerciseGroupButtonClicked()
        }
        awaitingGroupButton.setOnClickListener { controller.onAwaitingGroupButtonClicked() }
        learnedGroupButton.setOnClickListener { controller.onLearnedGroupButtonClicked() }
    }

    private fun setupLevelOfKnowledgeRangeBar() {
        with(levelOfKnowledgeRangeBar) {
            setOnRangeBarChangeListener(
                object : RangeBar.OnRangeBarChangeListener {
                    private var oldLeftPinValue: String? = leftPinValue
                    private var oldRightPinValue: String? = rightPinValue

                    override fun onTouchStarted(rangeBar: RangeBar?) {}

                    override fun onRangeChangeListener(
                        rangeBar: RangeBar?,
                        leftPinIndex: Int,
                        rightPinIndex: Int,
                        leftPinValue: String?,
                        rightPinValue: String?
                    ) {
                    }

                    override fun onTouchEnded(rangeBar: RangeBar?) {
                        if (isAnyPinValueChanged()) {
                            if (isLevelOfKnowledgeRangeListenerEnabled) {
                                val min = leftPinValue?.toInt() ?: return
                                val max = rightPinValue?.toInt() ?: return
                                controller.onLevelOfKnowledgeRangeChanged(min..max)
                            }
                            updateLevelOfKnowledgeRangeSelectorColors()
                        }
                        updateOldPinValues()
                    }

                    private fun isAnyPinValueChanged(): Boolean =
                        leftPinValue != oldLeftPinValue || rightPinValue != oldRightPinValue

                    private fun updateOldPinValues() {
                        oldLeftPinValue = leftPinValue
                        oldRightPinValue = rightPinValue
                    }
                })
            tickStart = viewModel.availableLevelOfKnowledgeRange.first.toFloat()
            tickEnd = viewModel.availableLevelOfKnowledgeRange.last.toFloat()
            val list = (tickStart.toInt()..tickEnd.toInt())
                .map(::getLevelOfKnowledgeColor)
            setConnectingLineColors(ArrayList(list))
            updateLevelOfKnowledgeRangeSelectorColors()
            tickTopLabels = viewModel.availableLevelOfKnowledgeRange
                .map { it.toString() }
                .toTypedArray()
        }
        viewModel.selectedLevelOfKnowledgeRange.observe { levelOfKnowledgeRange: IntRange ->
            isLevelOfKnowledgeRangeListenerEnabled = false
            levelOfKnowledgeRangeBar.setRangePinsByValue(
                levelOfKnowledgeRange.first.toFloat(),
                levelOfKnowledgeRange.last.toFloat()
            )
            isLevelOfKnowledgeRangeListenerEnabled = true
        }
    }

    private fun updateLevelOfKnowledgeRangeSelectorColors() {
        with(levelOfKnowledgeRangeBar) {
            leftSelectorColor = getLevelOfKnowledgeColor(leftPinValue.toInt())
            rightSelectorColor = getLevelOfKnowledgeColor(rightPinValue.toInt())
        }
    }

    private fun getLevelOfKnowledgeColor(levelOfKnowledge: Int): Int {
        val resId = when (levelOfKnowledge) {
            0 -> R.color.level_of_knowledge_unsatisfactory
            1 -> R.color.level_of_knowledge_poor
            2 -> R.color.level_of_knowledge_acceptable
            3 -> R.color.level_of_knowledge_satisfactory
            4 -> R.color.level_of_knowledge_good
            5 -> R.color.level_of_knowledge_very_good
            else -> R.color.level_of_knowledge_excellent
        }
        return ContextCompat.getColor(requireContext(), resId)
    }

    private fun setupLastAnswerFilter() {
        with(viewModel) {
            lastAnswerFromTimeAgo.observe { lastAnswerFromTimeAgo: DisplayedInterval? ->
                lastAnswerFromTextView.text =
                    if (lastAnswerFromTimeAgo == null) {
                        getString(R.string.zero_time).toLowerCase(Locale.ROOT)
                    } else {
                        val timeAgo: String =
                            lastAnswerFromTimeAgo.toString(requireContext()).toLowerCase(Locale.ROOT)
                        getString(R.string.time_ago, timeAgo)
                    }
            }
            lastAnswerToTimeAgo.observe { lastAnswerToTimeAgo: DisplayedInterval? ->
                lastAnswerToTextView.text =
                    if (lastAnswerToTimeAgo == null) {
                        getString(R.string.now).toLowerCase(Locale.ROOT)
                    } else {
                        val timeAgo: String =
                            lastAnswerToTimeAgo.toString(requireContext()).toLowerCase(Locale.ROOT)
                        getString(R.string.time_ago, timeAgo)
                    }
            }
        }
        lastAnswerFromButton.setOnClickListener {
            controller.onLastAnswerFromButtonClicked()
        }
        lastAnswerToButton.setOnClickListener {
            controller.onLastAnswerToButtonClicked()
        }
    }

    private fun setupNumberOfLaps() {
        viewModel.numberOfLaps.observe { numberOfLaps: Int ->
            val isInfinitely = numberOfLaps == Int.MAX_VALUE
            if (isInfinitely) {
                lapNumberTextView.setText(R.string.infinitely)
            } else {
                lapNumberTextView.text =
                    resources.getQuantityString(
                        R.plurals.number_of_laps_with_args,
                        numberOfLaps,
                        numberOfLaps
                    )
            }
        }
        lapsButton.setOnClickListener {
            controller.onLapsButtonClicked()
        }
    }

    private fun executeCommand(command: RepetitionSettingsController.Command) {
        when (command) {
            ShowNoCardIsReadyForRepetitionMessage -> {
                Toast.makeText(
                    requireContext(),
                    R.string.toast_text_no_card_matches_filter_conditions,
                    Toast.LENGTH_SHORT
                ).show()
            }
            is SetNamePresetDialogText -> {
                namePresetEditText.setText(command.text)
                namePresetEditText.selectAll()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.repetition_settings_actions, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_start_repetition -> {
                controller.onStartRepetitionMenuItemClicked()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.getBundle(STATE_KEY_NAME_PRESET_DIALOG)
            ?.let(namePresetDialog::onRestoreInstanceState)
    }

    override fun onPause() {
        super.onPause()
        controller.onFragmentPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (::namePresetDialog.isInitialized) {
            outState.putBundle(STATE_KEY_NAME_PRESET_DIALOG, namePresetDialog.onSaveInstanceState())
        }
    }

    companion object {
        const val STATE_KEY_NAME_PRESET_DIALOG = "STATE_KEY_NAME_PRESET_DIALOG"
    }
}