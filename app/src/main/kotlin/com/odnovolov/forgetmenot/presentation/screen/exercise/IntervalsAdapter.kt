package com.odnovolov.forgetmenot.presentation.screen.exercise

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.getBackgroundResForLevelOfKnowledge
import com.odnovolov.forgetmenot.presentation.screen.exercise.IntervalsAdapter.ViewHolder
import com.odnovolov.forgetmenot.presentation.screen.intervals.DisplayedInterval
import com.soywiz.klock.DateTimeSpan
import kotlinx.android.synthetic.main.item_level_of_knowledge.view.*

class IntervalsAdapter(
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<ViewHolder>() {
    var intervalItems: List<IntervalItem> = emptyList()
        set(value) {
            if (value != field) {
                field = value
                notifyDataSetChanged()
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_level_of_knowledge, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val intervalItem: IntervalItem = intervalItems[position]
        with(holder.itemView) {
            if (intervalItem.isSelected) {
                setBackgroundColor(
                    ContextCompat.getColor(context, R.color.current_level_of_knowledge_background)
                )
            } else {
                background = null
            }
            val backgroundRes = getBackgroundResForLevelOfKnowledge(intervalItem.levelOfKnowledge)
            levelOfKnowledgeTextView.setBackgroundResource(backgroundRes)
            levelOfKnowledgeTextView.text = intervalItem.levelOfKnowledge.toString()
            val displayedInterval = DisplayedInterval.fromDateTimeSpan(intervalItem.waitingPeriod)
            waitingPeriodTextView.text =
                "${displayedInterval.value} ${displayedInterval.intervalUnit}" // todo
            setLevelOfKnowledgeButton.setOnClickListener {
                onItemClick(intervalItem.levelOfKnowledge)
            }
        }
    }

    override fun getItemCount(): Int = intervalItems.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
}

data class IntervalItem(
    val levelOfKnowledge: Int,
    val waitingPeriod: DateTimeSpan,
    val isSelected: Boolean
)