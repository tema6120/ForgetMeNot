package com.odnovolov.forgetmenot.presentation.screen.repetitionsettings

import android.os.Bundle
import android.view.*
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.CheckBox
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.appyvet.materialrangebar.RangeBar
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.interactor.repetition.RepetitionSettings
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.entity.DisplayedInterval
import com.odnovolov.forgetmenot.presentation.common.preset.PresetFragment
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
    private var isLevelOfKnowledgeRangeListenerEnabled = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_repetition_settings, container, false)
    }

    override fun onAttachFragment(childFragment: Fragment) {
        if (childFragment is PresetFragment) {
            childFragment.controller = koinScope.get()
            childFragment.viewModel = koinScope.get()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMatchingCardsLabel()
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

    override fun onPause() {
        super.onPause()
        controller.onFragmentPause()
    }
}