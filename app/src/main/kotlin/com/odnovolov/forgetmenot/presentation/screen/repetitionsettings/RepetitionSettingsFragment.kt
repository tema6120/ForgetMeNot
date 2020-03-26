package com.odnovolov.forgetmenot.presentation.screen.repetitionsettings

import android.os.Bundle
import android.view.*
import android.view.View.*
import android.widget.CheckBox
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.appyvet.materialrangebar.RangeBar
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.interactor.repetition.RepetitionSettings
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.entity.DisplayedInterval
import com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.RepetitionSettingsController.Command.ShowNoCardIsReadyForRepetitionMessage
import kotlinx.android.synthetic.main.fragment_repetition_settings.*
import org.koin.android.ext.android.getKoin
import org.koin.androidx.viewmodel.scope.viewModel
import java.util.*

class RepetitionSettingsFragment : BaseFragment() {
    private val koinScope =
        getKoin().getOrCreateScope<RepetitionSettings>(REPETITION_SETTINGS_SCOPE_ID)
    private val viewModel: RepetitionSettingsViewModel by koinScope.viewModel(this)
    private val controller: RepetitionSettingsController by koinScope.inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_repetition_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupFilterGroups()
        setupLevelOfKnowledgeRangeBar()
        setupLastAnswerFilter()
        controller.commands.observe(::executeCommand)
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
            if (with(viewModel.availableLevelOfKnowledgeRange) { first == last }) {
                levelOfKnowledgeTitle.visibility = GONE
                visibility = GONE
                return
            }
            setOnRangeBarChangeListener(
                object : RangeBar.OnRangeBarChangeListener {
                    override fun onTouchStarted(rangeBar: RangeBar?) {}

                    override fun onRangeChangeListener(
                        rangeBar: RangeBar?,
                        leftPinIndex: Int,
                        rightPinIndex: Int,
                        leftPinValue: String?,
                        rightPinValue: String?
                    ) {
                        val min = leftPinValue?.toInt() ?: return
                        val max = rightPinValue?.toInt() ?: return
                        controller.onLevelOfKnowledgeRangeChanged(min..max)
                    }

                    override fun onTouchEnded(rangeBar: RangeBar?) {
                        leftSelectorColor = getLevelOfKnowledgeColor(leftPinValue.toInt())
                        rightSelectorColor = getLevelOfKnowledgeColor(rightPinValue.toInt())
                        setConnectingLineColors(arrayListOf(leftSelectorColor, rightSelectorColor))
                    }
                })
            tickStart = viewModel.availableLevelOfKnowledgeRange.first.toFloat()
            tickEnd = viewModel.availableLevelOfKnowledgeRange.last.toFloat()
            tickTopLabels = viewModel.availableLevelOfKnowledgeRange
                .map { it.toString() }
                .toTypedArray()
            setRangePinsByValue(
                viewModel.currentLevelOfKnowledgeRange.first.toFloat(),
                viewModel.currentLevelOfKnowledgeRange.last.toFloat()
            )
            levelOfKnowledgeTitle.visibility = VISIBLE
            visibility = VISIBLE
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
        return ContextCompat.getColor(context!!, resId)
    }

    private fun setupLastAnswerFilter() {
        with(viewModel) {
            lastAnswerFromTimeAgo.observe { lastAnswerFromTimeAgo: DisplayedInterval? ->
                lastAnswerFromTextView.text =
                    if (lastAnswerFromTimeAgo == null) {
                        getString(R.string.zero_time).toLowerCase(Locale.ROOT)
                    } else {
                        val timeAgo: String =
                            lastAnswerFromTimeAgo.toString(context!!).toLowerCase(Locale.ROOT)
                        getString(R.string.time_ago, timeAgo)
                    }
            }
            lastAnswerToTimeAgo.observe { lastAnswerToTimeAgo: DisplayedInterval? ->
                lastAnswerToTextView.text =
                    if (lastAnswerToTimeAgo == null) {
                        getString(R.string.now).toLowerCase(Locale.ROOT)
                    } else {
                        val timeAgo: String =
                            lastAnswerToTimeAgo.toString(context!!).toLowerCase(Locale.ROOT)
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