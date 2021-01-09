package com.odnovolov.forgetmenot.presentation.screen.intervals

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.getBrightGradeColorRes
import com.odnovolov.forgetmenot.presentation.common.getGradeColorRes
import com.odnovolov.forgetmenot.presentation.common.uncover
import com.odnovolov.forgetmenot.presentation.screen.intervals.IntervalAdapter.ViewHolder
import com.odnovolov.forgetmenot.presentation.screen.intervals.IntervalsEvent.*
import kotlinx.android.synthetic.main.item_interval.view.*
import kotlinx.android.synthetic.main.item_interval.view.indicatorLine as indicatorLine
import kotlinx.android.synthetic.main.item_interval_footer.view.*
import kotlinx.android.synthetic.main.item_interval_header.view.*
import kotlinx.android.synthetic.main.tip.view.*

class IntervalAdapter(
    private val controller: IntervalsController
) : ListAdapter<IntervalListItem, ViewHolder>(DiffCallback()) {
    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
        private const val TYPE_FOOTER = 2
    }

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is IntervalListItem.Header -> TYPE_HEADER
            is IntervalListItem.Footer -> TYPE_FOOTER
            else -> TYPE_ITEM
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = when (viewType) {
            TYPE_HEADER -> {
                layoutInflater.inflate(R.layout.item_interval_header, parent, false)
                    .apply {
                        intervalsSwitchFrame.setOnClickListener {
                            controller.dispatch(IntervalsSwitchToggled)
                        }
                    }
            }
            TYPE_FOOTER -> {
                layoutInflater.inflate(R.layout.item_interval_footer, parent, false)
                    .apply {
                        addIntervalButton.setOnClickListener {
                            controller.dispatch(AddIntervalButtonClicked)
                        }
                        removeIntervalButton.setOnClickListener {
                            controller.dispatch(RemoveIntervalButtonClicked)
                        }
                    }
            }
            else -> {
                layoutInflater.inflate(R.layout.item_interval, parent, false)
            }
        }
        return ViewHolder(viewType, view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val item = getItem(position)
        val view = viewHolder.itemView
        when (viewHolder.viewType) {
            TYPE_HEADER -> bind(item as IntervalListItem.Header, view)
            TYPE_FOOTER -> bind(item as IntervalListItem.Footer, view)
            else -> bind(item as IntervalListItem.IntervalWrapper, view)
        }
    }

    private fun bind(item: IntervalListItem.Header, view: View) {
        with(view) {
            if (item.tip != null) {
                if (tipStub != null) {
                    tipStub.inflate()
                    closeTipButton.setOnClickListener {
                        controller.dispatch(CloseTipButtonClicked)
                    }
                }
                val tipLayout = rootView.findViewById<View>(R.id.tipLayout)
                tipLayout.tipTextView.setText(item.tip.stringId)
                tipLayout.isVisible = true
            } else {
                if (tipStub == null) {
                    val tipLayout = rootView.findViewById<View>(R.id.tipLayout)
                    tipLayout.isVisible = false
                }
            }
            intervalsSwitch.isChecked = item.areIntervalsOn
            intervalsSwitch.setText(
                if (item.areIntervalsOn)
                    R.string.on else
                    R.string.off
            )
            intervalsSwitch.uncover()
        }
    }

    private fun bind(item: IntervalListItem.IntervalWrapper, view: View) {
        val interval = item.interval
        with(view) {
            indicatorLine.updateLayoutParams<ConstraintLayout.LayoutParams> {
                topToTop =
                    if (interval.grade == 0)
                        gradeTextView.id
                    else
                        ConstraintLayout.LayoutParams.PARENT_ID
            }
            setupGradeTextView(gradeTextView, interval.grade)
            val displayedInterval = DisplayedInterval.fromDateTimeSpan(interval.value)
            intervalButton.text = displayedInterval.toString(context)
            intervalButton.setOnClickListener {
                controller.dispatch(IntervalButtonClicked(interval.grade))
            }
        }
    }

    private fun bind(item: IntervalListItem.Footer, view: View) {
        with(view) {
            setupGradeTextView(excellentGradeTextView, item.excellentGrade)
            removeIntervalButton.isVisible = item.excellentGrade > 1
        }
    }

    private fun setupGradeTextView(gradeTextView: TextView, grade: Int) {
        val context = gradeTextView.context
        val gradeColorRes = getGradeColorRes(grade)
        val gradeColor: ColorStateList? = ContextCompat.getColorStateList(context, gradeColorRes)
        gradeTextView.backgroundTintList = gradeColor
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            val shadowColorRes = getBrightGradeColorRes(grade)
            val brightGradeColor: Int = ContextCompat.getColor(context, shadowColorRes)
            gradeTextView.outlineAmbientShadowColor = brightGradeColor
            gradeTextView.outlineSpotShadowColor = brightGradeColor
        }
        gradeTextView.text = grade.toString()
    }

    class ViewHolder(val viewType: Int, itemView: View) : RecyclerView.ViewHolder(itemView)

    class DiffCallback : ItemCallback<IntervalListItem>() {
        override fun areItemsTheSame(
            oldItem: IntervalListItem,
            newItem: IntervalListItem
        ): Boolean {
            return when {
                oldItem is IntervalListItem.Header && newItem is IntervalListItem.Header -> true
                oldItem is IntervalListItem.Footer && newItem is IntervalListItem.Footer -> true
                oldItem is IntervalListItem.IntervalWrapper &&
                        newItem is IntervalListItem.IntervalWrapper -> {
                    oldItem.interval.grade == newItem.interval.grade
                }
                else -> false
            }
        }

        override fun areContentsTheSame(
            oldItem: IntervalListItem,
            newItem: IntervalListItem
        ): Boolean {
            return if (oldItem is IntervalListItem.IntervalWrapper &&
                newItem is IntervalListItem.IntervalWrapper
            ) {
                oldItem.interval.grade == newItem.interval.grade
                        && oldItem.interval.value == newItem.interval.value
            } else {
                oldItem == newItem
            }
        }
    }
}