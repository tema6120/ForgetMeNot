package com.odnovolov.forgetmenot.presentation.screen.pronunciationplan

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.entity.PronunciationEvent
import com.odnovolov.forgetmenot.domain.entity.PronunciationEvent.*
import com.odnovolov.forgetmenot.presentation.common.SimpleRecyclerViewHolder
import com.odnovolov.forgetmenot.presentation.screen.pronunciationplan.PronunciationPlanUiEvent.PronunciationEventButtonClicked
import com.odnovolov.forgetmenot.presentation.screen.pronunciationplan.PronunciationPlanUiEvent.RemovePronunciationEventButtonClicked
import kotlinx.android.synthetic.main.item_pronunciation_event.view.*
import java.util.*
import kotlin.collections.ArrayList

class PronunciationEventAdapter(
    private val controller: PronunciationPlanController
) : RecyclerView.Adapter<SimpleRecyclerViewHolder>() {
    var itemTouchHelper: ItemTouchHelper? = null
    private var isDragging = false
    private var mutableItems: MutableList<PronunciationEventItem> = ArrayList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    private var pendingItems: List<PronunciationEventItem>? = null

    fun setItems(items: List<PronunciationEventItem>) {
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
            .inflate(R.layout.item_pronunciation_event, parent, false)
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
        val pronunciationEventItem: PronunciationEventItem = mutableItems[position]
        val pronunciationEvent: PronunciationEvent = pronunciationEventItem.pronunciationEvent
        with(viewHolder.itemView) {
            when (pronunciationEvent) {
                SpeakQuestion -> {
                    pronunciationEventTextView.text =
                        context.getString(R.string.pronunciation_event_speak_question)
                    val font = ResourcesCompat.getFont(context, R.font.nunito_extrabold)
                    pronunciationEventTextView.setTypeface(font, Typeface.BOLD)
                    pronunciationEventTextView.setTextColor(
                        ContextCompat.getColor(context, R.color.text_title)
                    )
                    speakIcon.visibility = VISIBLE
                    timeLineCenter.visibility = INVISIBLE
                }
                SpeakAnswer -> {
                    pronunciationEventTextView.text =
                        context.getString(R.string.pronunciation_event_speak_answer)
                    val font = ResourcesCompat.getFont(context, R.font.nunito_extrabold)
                    pronunciationEventTextView.setTypeface(font, Typeface.BOLD)
                    pronunciationEventTextView.setTextColor(
                        ContextCompat.getColor(context, R.color.text_title)
                    )
                    speakIcon.visibility = VISIBLE
                    timeLineCenter.visibility = INVISIBLE
                }
                is Delay -> {
                    val seconds = pronunciationEvent.timeSpan.seconds.toInt()
                    pronunciationEventTextView.text =
                        context.getString(R.string.pronunciation_event_delay_with_args, seconds)
                    pronunciationEventTextView.setTypeface(null, Typeface.ITALIC)
                    pronunciationEventTextView.setTextColor(
                        ContextCompat.getColor(context, R.color.text_description)
                    )
                    speakIcon.visibility = INVISIBLE
                    timeLineCenter.visibility = VISIBLE
                }
            }
            removePronunciationEventButton.isVisible = pronunciationEventItem.isRemovable
            pronunciationEventButton.setOnClickListener {
                if (!isDragging) {
                    controller.dispatch(PronunciationEventButtonClicked(position))
                }
            }
            removePronunciationEventButton.setOnClickListener {
                if (!isDragging) {
                    controller.dispatch(RemovePronunciationEventButtonClicked(position))
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return mutableItems.size
    }
}