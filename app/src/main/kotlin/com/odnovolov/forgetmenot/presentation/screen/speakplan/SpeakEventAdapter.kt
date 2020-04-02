package com.odnovolov.forgetmenot.presentation.screen.speakplan

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.entity.SpeakEvent
import com.odnovolov.forgetmenot.domain.entity.SpeakEvent.*
import com.odnovolov.forgetmenot.presentation.screen.speakplan.SpeakEventAdapter.ViewHolder
import kotlinx.android.synthetic.main.item_speak_event.view.*

class SpeakEventAdapter(
    private val controller: SpeakPlanController
) : ListAdapter<SpeakEventItem, ViewHolder>(DiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_speak_event, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val speakEventItem: SpeakEventItem = getItem(position)
        val speakEvent: SpeakEvent = speakEventItem.speakEvent
        with(viewHolder.itemView) {
            when (speakEvent) {
                is SpeakQuestion -> {
                    speakEventTextView.text = context.getString(R.string.speak_event_speak_question)
                    speakEventTextView.setTypeface(null, Typeface.BOLD)
                    speakEventTextView.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.textPrimary
                        )
                    )
                    speakIcon.visibility = VISIBLE
                    timeLine2.visibility = INVISIBLE
                }
                is SpeakAnswer -> {
                    speakEventTextView.text = context.getString(R.string.speak_event_speak_answer)
                    speakEventTextView.setTypeface(null, Typeface.BOLD)
                    speakEventTextView.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.textPrimary
                        )
                    )
                    speakIcon.visibility = VISIBLE
                    timeLine2.visibility = INVISIBLE
                }
                is Delay -> {
                    val seconds = speakEvent.timeSpan.seconds.toInt()
                    speakEventTextView.text =
                        context.getString(R.string.speak_event_delay_with_args, seconds)
                    speakEventTextView.setTypeface(null, Typeface.ITALIC)
                    speakEventTextView.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.textSecondary
                        )
                    )
                    speakIcon.visibility = INVISIBLE
                    timeLine2.visibility = VISIBLE
                }
            }
            removeSpeakEventButton.visibility = if (speakEventItem.isRemovable) VISIBLE else GONE
            speakEventButton.setOnClickListener {
                controller.onSpeakEventButtonClicked(speakEvent.id)
            }
            removeSpeakEventButton.setOnClickListener {
                controller.onRemoveSpeakEventButtonClicked(speakEvent.id)
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    class DiffCallback : DiffUtil.ItemCallback<SpeakEventItem>() {
        override fun areItemsTheSame(oldItem: SpeakEventItem, newItem: SpeakEventItem): Boolean {
            return oldItem.speakEvent.id == newItem.speakEvent.id
        }

        override fun areContentsTheSame(oldItem: SpeakEventItem, newItem: SpeakEventItem): Boolean {
            return oldItem == newItem
        }
    }
}