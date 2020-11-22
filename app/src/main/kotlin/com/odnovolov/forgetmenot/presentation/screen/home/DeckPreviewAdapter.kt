package com.odnovolov.forgetmenot.presentation.screen.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.SimpleRecyclerViewHolder
import com.odnovolov.forgetmenot.presentation.common.highlight
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
                layoutInflater.inflate(R.layout.item_deck_preview, parent, false)
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
        itemView.lastOpenedValueTextView.text = deckPreview.lastOpenedAt
        updateDeckItemSelectionState(itemView, deckPreview.deckId)
        itemViewDeckIdMap[itemView] = deckPreview.deckId
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