package com.odnovolov.forgetmenot.presentation.screen.home

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.SimpleRecyclerViewHolder
import com.odnovolov.forgetmenot.presentation.common.highlight
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.AsyncFrameLayout
import com.odnovolov.forgetmenot.presentation.screen.home.DeckListItem.DeckPreview
import com.odnovolov.forgetmenot.presentation.screen.home.HomeEvent.*
import kotlinx.android.synthetic.main.item_deck_preview.view.*

class DeckPreviewAdapter(
    private val controller: HomeController,
    private val setupHeader: (View) -> Unit
) : ListAdapter<DeckListItem, SimpleRecyclerViewHolder>(DiffCallback()) {
    var deckSelection: DeckSelection? = null
        set(value) {
            field = value
            itemViewDeckIdMap.forEach { (itemView: View, deckId: Long) ->
                updateDeckItemSelectionState(itemView, deckId)
            }
        }

    private var itemViewDeckIdMap = HashMap<View, Long>()

    private fun updateDeckItemSelectionState(itemView: View, deckId: Long) {
        val isItemSelected: Boolean? = deckSelection?.run {
            deckId in selectedDeckIds
        }
        itemView.isSelected = isItemSelected == true
        itemView.deckOptionButton.isVisible = isItemSelected == null
        itemView.deckSelector.isVisible = isItemSelected != null
    }

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
        private const val TYPE_FOOTER = 2
    }

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            DeckListItem.Header -> TYPE_HEADER
            DeckListItem.Footer -> TYPE_FOOTER
            else -> TYPE_ITEM
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleRecyclerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = when (viewType) {
            TYPE_HEADER -> {
                layoutInflater.inflate(R.layout.item_deck_preview_header, parent, false)
                    .also(setupHeader)
            }
            TYPE_FOOTER -> {
                layoutInflater.inflate(R.layout.item_deck_preview_footer, parent, false)
            }
            else -> {
                val layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                AsyncFrameLayout(layoutParams, parent.context).apply {
                    inflateAsync(R.layout.item_deck_preview)
                    invokeWhenInflated {
                        // with async inflation, parameter 'fontFamily' in xml causes InflateException
                        // so we set font on ui thread programmatically
                        deckNameTextView.setTypeface(
                            ResourcesCompat.getFont(context, R.font.comfortaa),
                            Typeface.BOLD
                        )
                    }
                }
            }
        }
        return SimpleRecyclerViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: SimpleRecyclerViewHolder, position: Int) {
        val deckListItem = getItem(position)
        when (deckListItem) {
            DeckListItem.Header, DeckListItem.Footer -> return
        }
        val deckPreview = deckListItem as DeckPreview
        (viewHolder.itemView as AsyncFrameLayout).invokeWhenInflated {
            val itemView = viewHolder.itemView
            itemView.deckButton.setOnClickListener {
                controller.dispatch(DeckButtonClicked(deckPreview.deckId))
            }
            itemView.deckButton.setOnLongClickListener {
                controller.dispatch(DeckButtonLongClicked(deckPreview.deckId))
                true
            }
            itemView.deckNameTextView.text = if (deckPreview.searchMatchingRanges != null) {
                deckPreview.deckName
                    .highlight(deckPreview.searchMatchingRanges, itemView.context)
            } else {
                deckPreview.deckName
            }
            itemView.deckOptionButton.setOnClickListener {
                controller.dispatch(DeckOptionButtonClicked(deckPreview.deckId))
            }
            itemView.deckSelector.setOnClickListener {
                controller.dispatch(DeckSelectorClicked(deckPreview.deckId))
            }
            itemView.avgLapsValueTextView.text = deckPreview.averageLaps
            itemView.learnedValueTextView.text =
                "${deckPreview.learnedCount}/${deckPreview.totalCount}"
            itemView.taskValueTextView.text =
                deckPreview.numberOfCardsReadyForExercise?.toString() ?: "-"
            itemView.taskValueTextView.setTextColor(
                getTaskColor(deckPreview.numberOfCardsReadyForExercise, itemView.context)
            )
            itemView.lastTestedValueTextView.text = deckPreview.lastOpenedAt
            updateDeckItemSelectionState(itemView, deckPreview.deckId)
            itemViewDeckIdMap[itemView] = deckPreview.deckId
        }
    }

    private var colorNotHasTask: Int? = null
    private var colorHasTask: Int? = null

    private fun getTaskColor(numberOfCardsReadyForExercise: Int?, context: Context): Int {
        return if (numberOfCardsReadyForExercise == null || numberOfCardsReadyForExercise == 0) {
            if (colorNotHasTask == null) {
                colorNotHasTask = ContextCompat.getColor(context, R.color.textPrimary)
            }
            colorNotHasTask!!
        } else {
            if (colorHasTask == null) {
                colorHasTask = ContextCompat.getColor(context, R.color.text_task)
            }
            colorHasTask!!
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<DeckListItem>() {
        override fun areItemsTheSame(oldItem: DeckListItem, newItem: DeckListItem): Boolean {
            return when {
                oldItem === newItem -> true
                oldItem is DeckPreview && newItem is DeckPreview -> {
                    oldItem.deckId == newItem.deckId
                }
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: DeckListItem, newItem: DeckListItem): Boolean {
            return oldItem == newItem
        }
    }
}