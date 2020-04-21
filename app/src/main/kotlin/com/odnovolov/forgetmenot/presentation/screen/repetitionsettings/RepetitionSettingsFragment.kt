package com.odnovolov.forgetmenot.presentation.screen.repetitionsettings

import android.os.Bundle
import android.view.*
import android.widget.CheckBox
import androidx.core.content.ContextCompat
import com.appyvet.materialrangebar.RangeBar
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.entity.DisplayedInterval
import com.odnovolov.forgetmenot.presentation.common.inflateAsync
import com.odnovolov.forgetmenot.presentation.common.needToCloseDiScope
import com.odnovolov.forgetmenot.presentation.common.showToast
import com.odnovolov.forgetmenot.presentation.common.uncover
import com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.RepetitionSettingsController.Command.ShowNoCardIsReadyForRepetitionMessage
import com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.RepetitionSettingsEvent.*
import kotlinx.android.synthetic.main.fragment_repetition_settings.*
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class RepetitionSettingsFragment : BaseFragment() {
    init {
        RepetitionSettingsDiScope.reopenIfClosed()
    }

    private lateinit var diScope: RepetitionSettingsDiScope
    private var controller: RepetitionSettingsController? = null
    private var isLevelOfKnowledgeRangeListenerEnabled = true
    private var isInflated = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return if (savedInstanceState == null) {
            inflater.inflateAsync(R.layout.fragment_repetition_settings, ::onViewInflated)
        } else {
            inflater.inflate(R.layout.fragment_repetition_settings, container, false)
        }
    }

    private fun onViewInflated() {
        isInflated = true
        setupIfReady()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState != null) {
            isInflated = true
        }
        viewCoroutineScope!!.launch {
            diScope = RepetitionSettingsDiScope.get()
            controller = diScope.controller
            setupIfReady()
        }
    }

    private fun setupIfReady() {
        if (viewCoroutineScope == null || controller == null || !isInflated) return
        presetView.inject(diScope.presetController, diScope.presetViewModel)
        setupView()
        observeViewModel()
        controller!!.commands.observe(::executeCommand)
    }

    private fun setupView() {
        setupFilterGroups()
        setupLevelOfKnowledgeRangeBar()
        setupLastAnswerFilter()
        setupNumberOfLaps()
    }

    private fun setupFilterGroups() {
        availableForExerciseGroupButton.setOnClickListener {
            controller?.dispatch(AvailableForExerciseGroupButtonClicked)
        }
        awaitingGroupButton.setOnClickListener { controller?.dispatch(AwaitingGroupButtonClicked) }
        learnedGroupButton.setOnClickListener { controller?.dispatch(LearnedGroupButtonClicked) }
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
                                controller?.dispatch(LevelOfKnowledgeRangeChanged(min..max))
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
        lastAnswerFromButton.setOnClickListener {
            controller?.dispatch(LastAnswerFromButtonClicked)
        }
        lastAnswerToButton.setOnClickListener { controller?.dispatch(LastAnswerToButtonClicked) }
    }

    private fun setupNumberOfLaps() {
        lapsButton.setOnClickListener { controller?.dispatch(LapsButtonClicked) }
    }

    private fun observeViewModel() {
        with(diScope.viewModel) {
            matchingCardsNumber.observe { matchingCardsNumber: Int ->
                matchingCardsNumberTextView.text = matchingCardsNumber.toString()
                matchingCardsLabelTextView.text = resources.getQuantityString(
                    R.plurals.matching_cards_number_label,
                    matchingCardsNumber
                )
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
            availableLevelOfKnowledgeRange.let(::adaptLevelOfKnowledgeBarToAvailableRange)
            selectedLevelOfKnowledgeRange.observe { levelOfKnowledgeRange: IntRange ->
                isLevelOfKnowledgeRangeListenerEnabled = false
                levelOfKnowledgeRangeBar.setRangePinsByValue(
                    levelOfKnowledgeRange.first.toFloat(),
                    levelOfKnowledgeRange.last.toFloat()
                )
                isLevelOfKnowledgeRangeListenerEnabled = true
            }
            lastAnswerFromTimeAgo.observe { lastAnswerFromTimeAgo: DisplayedInterval? ->
                lastAnswerFromTextView.text =
                    if (lastAnswerFromTimeAgo == null) {
                        getString(R.string.zero_time).toLowerCase(Locale.ROOT)
                    } else {
                        val timeAgo: String = lastAnswerFromTimeAgo.toString(requireContext())
                            .toLowerCase(Locale.ROOT)
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
            numberOfLaps.observe { numberOfLaps: Int ->
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
        }
    }

    private fun updateCheckBox(checkBox: CheckBox, isChecked: Boolean) {
        with(checkBox) {
            setChecked(isChecked)
            checkBox.uncover()
        }
    }

    private fun adaptLevelOfKnowledgeBarToAvailableRange(availableLevelOfKnowledgeRange: IntRange) {
        with(levelOfKnowledgeRangeBar) {
            tickStart = availableLevelOfKnowledgeRange.first.toFloat()
            tickEnd = availableLevelOfKnowledgeRange.last.toFloat()
            tickTopLabels = availableLevelOfKnowledgeRange
                .map { it.toString() }
                .toTypedArray()
            val list = (tickStart.toInt()..tickEnd.toInt())
                .map(::getLevelOfKnowledgeColor)
            setConnectingLineColors(ArrayList(list))
            updateLevelOfKnowledgeRangeSelectorColors()
        }
    }

    private fun updateLevelOfKnowledgeRangeSelectorColors() {
        with(levelOfKnowledgeRangeBar) {
            leftSelectorColor = getLevelOfKnowledgeColor(leftPinValue.toInt())
            rightSelectorColor = getLevelOfKnowledgeColor(rightPinValue.toInt())
        }
    }

    private fun executeCommand(command: RepetitionSettingsController.Command) {
        when (command) {
            ShowNoCardIsReadyForRepetitionMessage -> {
                showToast(R.string.toast_text_no_card_matches_filter_conditions)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.repetition_settings_actions, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_start_repetition -> {
                controller?.dispatch(StartRepetitionMenuItemClicked)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isInflated = false
    }

    override fun onDestroy() {
        super.onDestroy()
        if (needToCloseDiScope()) {
            RepetitionSettingsDiScope.close()
        }
    }
}