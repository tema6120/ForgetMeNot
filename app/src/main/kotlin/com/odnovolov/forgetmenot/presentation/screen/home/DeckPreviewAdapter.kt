package com.odnovolov.forgetmenot.presentation.screen.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.screen.home.DeckPreviewAdapter.ViewHolder
import com.odnovolov.forgetmenot.presentation.screen.home.HomeEvent.*
import kotlinx.android.synthetic.main.item_deck_preview.view.*

class DeckPreviewAdapter(
    private val controller: HomeController
) : ListAdapter<DeckPreview, ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_deck_preview, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        with(viewHolder.itemView) {
            val deckPreview: DeckPreview = getItem(position)
            deckButton.setOnClickListener {
                controller.dispatch(DeckButtonClicked(deckPreview.deckId))
            }
            deckButton.setOnLongClickListener {
                controller.dispatch(DeckButtonLongClicked(deckPreview.deckId))
                true
            }
            deckNameTextView.text = deckPreview.deckName
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

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    class DiffCallback : DiffUtil.ItemCallback<DeckPreview>() {
        override fun areItemsTheSame(oldItem: DeckPreview, newItem: DeckPreview): Boolean {
            return oldItem.deckId == newItem.deckId
        }

        override fun areContentsTheSame(oldItem: DeckPreview, newItem: DeckPreview): Boolean {
            return oldItem == newItem
        }
    }
}