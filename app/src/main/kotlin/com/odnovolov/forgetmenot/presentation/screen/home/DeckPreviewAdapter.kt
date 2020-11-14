package com.odnovolov.forgetmenot.presentation.screen.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
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
        with(viewHolder.itemView) {
            val deckListItem = getItem(position)
            when(deckListItem) {
                DeckListItem.Header, DeckListItem.Footer -> return
            }
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
            deckSelector.setOnClickListener {
                controller.dispatch(DeckSelectorClicked(deckPreview.deckId))
            }
            avgLapsValueTextView.text = "%.1f".format(deckPreview.averageLaps)
            val progress = "${deckPreview.learnedCount}/${deckPreview.totalCount}"
            learnedValueTextView.text = progress
            taskValueTextView.text = deckPreview.numberOfCardsReadyForExercise?.toString() ?: "-"
            lastOpenedValueTextView.text = deckPreview.lastOpened?.format("MMM d") ?: "-"
            isSelected = deckPreview.isSelected == true
            deckOptionButton.isVisible = deckPreview.isSelected == null
            deckSelector.isVisible = deckPreview.isSelected != null
        }
    }

    private fun showOptionMenu(anchor: View, deckId: Long) {
        PopupMenu(anchor.context, anchor).apply {
            inflate(R.menu.deck_preview_actions)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.startExerciseMenuItem -> {
                        controller.dispatch(StartExerciseDeckOptionSelected(deckId))
                        true
                    }
                    R.id.repetitionModeMenuItem -> {
                        controller.dispatch(AutoplayDeckOptionSelected(deckId))
                        true
                    }
                    R.id.setupDeckMenuItem -> {
                        controller.dispatch(SetupDeckOptionSelected(deckId))
                        true
                    }
                    R.id.exportMenuItem -> {
                        controller.dispatch(ExportDeckOptionSelected(deckId))
                        true
                    }
                    R.id.removeDeckMenuItem -> {
                        controller.dispatch(RemoveDeckOptionSelected(deckId))
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