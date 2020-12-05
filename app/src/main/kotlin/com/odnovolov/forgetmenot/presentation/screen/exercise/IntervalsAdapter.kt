package com.odnovolov.forgetmenot.presentation.screen.exercise

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.SimpleRecyclerViewHolder
import com.odnovolov.forgetmenot.presentation.common.entity.DisplayedInterval
import com.odnovolov.forgetmenot.presentation.common.getGradeColorRes
import com.soywiz.klock.DateTimeSpan
import kotlinx.android.synthetic.main.item_grade.view.*

class IntervalsAdapter(
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<SimpleRecyclerViewHolder>() {
    var intervalItems: List<IntervalItem> = emptyList()
        set(value) {
            if (value != field) {
                field = value
                updateSelectedPosition()
                notifyDataSetChanged()
            }
        }

    private var selectedPosition: Int = -1

    private fun updateSelectedPosition() {
        selectedPosition = intervalItems.indexOfFirst { it.isSelected }
    }

    override fun getItemCount(): Int = intervalItems.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleRecyclerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_grade, parent, false)
        return SimpleRecyclerViewHolder(view)
    }

    override fun onBindViewHolder(holder: SimpleRecyclerViewHolder, position: Int) {
        val intervalItem: IntervalItem = intervalItems[position]
        with(holder.itemView) {
            isSelected = intervalItem.isSelected
            setOnClickListener {
                onItemClick(intervalItem.grade)
            }
            val gradeColor: Int = ContextCompat.getColor(
                context,
                getGradeColorRes(intervalItem.grade)
            )
            gradeIcon.background.setTint(gradeColor)
            gradeIcon.text = intervalItem.grade.toString()
            val displayedInterval = DisplayedInterval.fromDateTimeSpan(intervalItem.waitingPeriod)
            waitingPeriodTextView.text = displayedInterval.toString(context)
        }
    }
}

data class IntervalItem(
    val grade: Int,
    val waitingPeriod: DateTimeSpan,
    val isSelected: Boolean
)