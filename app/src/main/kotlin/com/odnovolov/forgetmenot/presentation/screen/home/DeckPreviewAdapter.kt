package com.odnovolov.forgetmenot.presentation.screen.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
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
    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    override fun getItemViewType(position: Int): Int =
        if (getItem(position) == DeckListItem.Header)
            TYPE_HEADER else
            TYPE_ITEM

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleRecyclerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = if (viewType == TYPE_HEADER) {
            layoutInflater.inflate(R.layout.item_deck_preview_header, parent, false)
                .also(setupHeader)
        } else {
            layoutInflater.inflate(R.layout.item_deck_preview, parent, false)
        }
        return SimpleRecyclerViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: SimpleRecyclerViewHolder, position: Int) {
        with(viewHolder.itemView) {
            val deckListItem = getItem(position)
            if (deckListItem == DeckListItem.Header) return
            val deckPreview = deckListItem as DeckPreview
            deckButton.setOnClickListener {
                controller.dispatch(DeckButtonClicked(deckPreview.deckId))
            }
            deckButton.setOnLongClickListener {
                controller.dispatch(DeckButtonLongClicked(deckPreview.deckId))
                true
            }
            deckNameTextView.text = if (deckPreview.searchMatchingRanges != null) {
                deckPreview.deckName
                    .highlight(deckPreview.searchMatchingRanges, context)
            } else {
                deckPreview.deckName
            }
            deckOptionButton.setOnClickListener { view: View ->
                showOptionMenu(view, deckPreview.deckId)
            }
            avgLapsValueTextView.text = "%.1f".format(deckPreview.averageLaps)
            val progress = "${deckPreview.learnedCount}/${deckPreview.totalCount}"
            learnedValueTextView.text = progress
            taskValueTextView.text = deckPreview.numberOfCardsReadyForExercise?.toString() ?: "-"
            lastOpenedValueTextView.text = deckPreview.lastOpened?.format("MMM d") ?: "-"
            isSelected = deckPreview.isSelected
        }
    }

    private fun showOptionMenu(anchor: View, deckId: Long) {
        PopupMenu(anchor.context, anchor).apply {
            inflate(R.menu.deck_preview_actions)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.repetitionModeMenuItem -> {
                        controller.dispatch(RepetitionModeMenuItemClicked(deckId))
                        true
                    }
                    R.id.setupDeckMenuItem -> {
                        controller.dispatch(SetupDeckMenuItemClicked(deckId))
                        true
                    }
                    R.id.exportMenuItem -> {
                        controller.dispatch(ExportMenuItemClicked(deckId))
                        true
                    }
                    R.id.removeDeckMenuItem -> {
                        controller.dispatch(RemoveDeckMenuItemClicked(deckId))
                        true
                    }
                    else -> false
                }
            }
            show()
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