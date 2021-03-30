package com.odnovolov.forgetmenot.presentation.screen.home.noexercisecard

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.entity.IntervalScheme
import com.odnovolov.forgetmenot.domain.toDateTimeSpan
import com.odnovolov.forgetmenot.presentation.common.base.BaseDialogFragment
import com.odnovolov.forgetmenot.presentation.common.createDialog
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.getIntervalsDisplayText
import com.odnovolov.forgetmenot.presentation.screen.home.HomeController
import com.odnovolov.forgetmenot.presentation.screen.home.HomeDiScope
import com.odnovolov.forgetmenot.presentation.screen.home.HomeEvent.GoToDeckSettingsButtonClicked
import com.soywiz.klock.DateTime
import com.soywiz.klock.DateTimeSpan
import kotlinx.android.synthetic.main.dialog_no_exercise_card.view.*
import kotlinx.coroutines.launch

class NoExerciseCardDialog : BaseDialogFragment() {
    init {
        HomeDiScope.reopenIfClosed()
    }

    private var controller: HomeController? = null
    private lateinit var contentView: View

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog()
        contentView = View.inflate(requireContext(), R.layout.dialog_no_exercise_card, null)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = HomeDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            observeViewModel(diScope.noExerciseCardViewModel)
        }
        return createDialog(contentView)
    }

    private fun setupView() {
        with(contentView) {
            closeButton.setOnClickListener {
                dismiss()
            }
            goToDeckSettingsButton.setOnClickListener {
                controller?.dispatch(GoToDeckSettingsButtonClicked)
                dismiss()
            }
        }
    }

    private fun observeViewModel(viewModel: NoExerciseCardViewModel) {
        with(contentView) {
            val intervalScheme: IntervalScheme? =
                viewModel.relatedDeck?.exercisePreference?.intervalScheme
            description1TextView.text = composeParagraph1(intervalScheme)
            timeTextView.text =
                generateRemainingTimeString(viewModel.timeWhenTheFirstCardWillBeAvailable)
            goToDeckSettingsButton.isVisible = viewModel.relatedDeck != null
        }
    }

    private fun composeParagraph1(intervalScheme: IntervalScheme?): String {
        val part1 = if (intervalScheme == null) {
            getString(R.string.description_no_exercise_card_dialog_paragraph_1_part_1)
        } else {
            val intervalsDisplayText = getIntervalsDisplayText(intervalScheme, requireContext())
            getString(
                R.string.description_no_exercise_card_dialog_paragraph_1_part_1_with_args,
                intervalsDisplayText
            )
        }
        val part2 = getString(R.string.description_no_exercise_card_dialog_paragraph_1_part_2)
        return part1 + part2
    }

    private fun generateRemainingTimeString(timeWhenTheFirstCardWillBeAvailable: DateTime?): String {
        if (timeWhenTheFirstCardWillBeAvailable == null) {
            return "-"
        }
        val remainingTime: DateTimeSpan = (timeWhenTheFirstCardWillBeAvailable - DateTime.now())
            .toDateTimeSpan()
        return when {
            remainingTime.daysIncludingWeeks > 0 -> {
                val daysString = timeStringOf(remainingTime.daysIncludingWeeks, R.plurals.days)
                val hoursString = timeStringOf(remainingTime.hours, R.plurals.hours)
                "$daysString & $hoursString"
            }
            remainingTime.hours > 0 -> {
                val hoursString = timeStringOf(remainingTime.hours, R.plurals.hours)
                val minutesString = timeStringOf(remainingTime.minutes, R.plurals.minutes)
                "$hoursString & $minutesString"
            }
            else -> {
                val minutesString = timeStringOf(remainingTime.minutes, R.plurals.minutes)
                val secondsString = timeStringOf(remainingTime.seconds, R.plurals.seconds)
                "$minutesString & $secondsString"
            }
        }
    }

    private fun timeStringOf(timeValue: Int, pluralRes: Int): String {
        return resources.getQuantityString(pluralRes, timeValue, timeValue)
    }
}