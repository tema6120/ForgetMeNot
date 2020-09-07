package com.odnovolov.forgetmenot.presentation.screen.speakplan

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.entity.SpeakEvent
import com.odnovolov.forgetmenot.domain.entity.SpeakEvent.*
import com.odnovolov.forgetmenot.presentation.common.SimpleRecyclerViewHolder
import com.odnovolov.forgetmenot.presentation.screen.speakplan.SpeakPlanUiEvent.RemoveSpeakEventButtonClicked
import com.odnovolov.forgetmenot.presentation.screen.speakplan.SpeakPlanUiEvent.SpeakEventButtonClicked
import kotlinx.android.synthetic.main.item_speak_event.view.*
import java.util.*
import kotlin.collections.ArrayList

class SpeakEventAdapter(
    private val controller: SpeakPlanController
) : RecyclerView.Adapter<SimpleRecyclerViewHolder>() {
    var itemTouchHelper: ItemTouchHelper? = null
    private var isDragging = false
    private var mutableItems: MutableList<SpeakEventItem> = ArrayList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    private var pendingItems: List<SpeakEventItem>? = null

    fun setItems(items: List<SpeakEventItem>) {
        if (isDragging) {
            pendingItems = items
        } else {
            mutableItems = items.toMutableList()
        }
    }

    fun onItemMove(fromPosition: Int, toPosition: Int) {
        Collections.swap(mutableItems, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleRecyclerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_speak_event, parent, false)
        val viewHolder = SimpleRecyclerViewHolder(view)
        view.dragHandleButton.setOnTouchListener { _, event ->
            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                itemTouchHelper?.startDrag(viewHolder)
            }
            false
        }
        return viewHolder
    }

    fun onStartDragging(viewHolder: ViewHolder) {
        isDragging = true
        updateDraggingView(viewHolder.itemView)
    }

    fun onStopDragging(viewHolder: ViewHolder) {
        isDragging = false
        pendingItems?.let(::setItems)
        pendingItems = null
        updateDraggingView(viewHolder.itemView)
    }

    private fun updateDraggingView(draggingView: View) {
        draggingView.background = if (isDragging) ColorDrawable(Color.WHITE) else null
    }

    override fun onBindViewHolder(viewHolder: SimpleRecyclerViewHolder, position: Int) {
        val speakEventItem: SpeakEventItem = mutableItems[position]
        val speakEvent: SpeakEvent = speakEventItem.speakEvent
        with(viewHolder.itemView) {
            when (speakEvent) {
                SpeakQuestion -> {
                    speakEventTextView.text = context.getString(R.string.speak_event_speak_question)
                    speakEventTextView.setTypeface(null, Typeface.BOLD)
                    speakEventTextView.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.textPrimary
                        )
                    )
                    speakIcon.visibility = VISIBLE
                    timeLineCenter.visibility = INVISIBLE
                }
                SpeakAnswer -> {
                    speakEventTextView.text = context.getString(R.string.speak_event_speak_answer)
                    speakEventTextView.setTypeface(null, Typeface.BOLD)
                    speakEventTextView.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.textPrimary
                        )
                    )
                    speakIcon.visibility = VISIBLE
                    timeLineCenter.visibility = INVISIBLE
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
                    timeLineCenter.visibility = VISIBLE
                }
            }
            removeSpeakEventButton.visibility = if (speakEventItem.isRemovable) VISIBLE else GONE
            speakEventButton.setOnClickListener {
                if (!isDragging) {
                    controller.dispatch(SpeakEventButtonClicked(position))
                }
            }
            removeSpeakEventButton.setOnClickListener {
                if (!isDragging) {
                    controller.dispatch(RemoveSpeakEventButtonClicked(position))
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return mutableItems.size
    }
}