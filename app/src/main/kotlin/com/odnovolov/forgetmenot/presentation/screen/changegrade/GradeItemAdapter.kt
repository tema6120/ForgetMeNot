package com.odnovolov.forgetmenot.presentation.screen.changegrade

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.SimpleRecyclerViewHolder
import com.odnovolov.forgetmenot.presentation.common.dp
import com.odnovolov.forgetmenot.presentation.common.getGradeColorRes
import com.odnovolov.forgetmenot.presentation.screen.intervals.DisplayedInterval
import com.soywiz.klock.DateTimeSpan
import kotlinx.android.synthetic.main.item_grade.view.*

class GradeItemAdapter(
    private val items: List<GradeItem>,
    private val onGradeSelected: (grade: Int) -> Unit
) : RecyclerView.Adapter<SimpleRecyclerViewHolder>() {
    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleRecyclerViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_change_grade, parent, false)
        return SimpleRecyclerViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SimpleRecyclerViewHolder, position: Int) {
        val (grade: Int, waitingPeriod: DateTimeSpan?) = items[position]
        with(holder.itemView) {
            setOnClickListener { onGradeSelected(grade) }
            val gradeColorRes = getGradeColorRes(grade)
            gradeIcon.backgroundTintList = ContextCompat.getColorStateList(context, gradeColorRes)
            gradeIcon.text = grade.toString()
            waitingPeriodTextView.text =
                if (waitingPeriod != null) {
                    DisplayedInterval.fromDateTimeSpan(waitingPeriod).toString(context)
                } else {
                    "- - -"
                }
            updateLayoutParams<MarginLayoutParams> {
                val marginBottom: Int = if (position == items.lastIndex) 16.dp else 0
                updateMargins(bottom = marginBottom)
            }
        }
    }
}