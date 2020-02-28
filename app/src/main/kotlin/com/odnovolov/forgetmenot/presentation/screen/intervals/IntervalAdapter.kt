package com.odnovolov.forgetmenot.presentation.screen.intervals

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.R.layout
import com.odnovolov.forgetmenot.domain.entity.Interval
import com.odnovolov.forgetmenot.presentation.common.getBackgroundResForLevelOfKnowledge
import com.odnovolov.forgetmenot.presentation.screen.intervals.IntervalAdapter.ViewHolder
import kotlinx.android.synthetic.main.item_interval.view.*

class IntervalAdapter(private val controller: IntervalsController) :
    ListAdapter<Interval, ViewHolder>(DiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(layout.item_interval, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.itemView) {
            val interval: Interval = getItem(position)
            val backgroundRes = getBackgroundResForLevelOfKnowledge(interval.targetLevelOfKnowledge)
            levelOfKnowledgeTextView.setBackgroundResource(backgroundRes)
            levelOfKnowledgeTextView.text = interval.targetLevelOfKnowledge.toString()
            val displayedInterval = DisplayedInterval.fromDateTimeSpan(interval.value)
            intervalTextView.text = "${displayedInterval.value} ${displayedInterval.intervalUnit}"
            modifyIntervalButton.setOnClickListener {
                controller.onModifyIntervalButtonClicked(interval.targetLevelOfKnowledge)
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    class DiffCallback : ItemCallback<Interval>() {
        override fun areItemsTheSame(oldItem: Interval, newItem: Interval): Boolean {
            return oldItem.targetLevelOfKnowledge == newItem.targetLevelOfKnowledge
        }

        override fun areContentsTheSame(oldItem: Interval, newItem: Interval): Boolean {
            return oldItem.targetLevelOfKnowledge == newItem.targetLevelOfKnowledge
                    && oldItem.value == newItem.value
        }
    }
}