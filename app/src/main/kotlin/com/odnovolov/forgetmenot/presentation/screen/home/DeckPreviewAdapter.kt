package com.odnovolov.forgetmenot.presentation.screen.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.SimpleRecyclerViewHolder
import com.odnovolov.forgetmenot.presentation.screen.home.DeckPreviewAdapter.Item
import com.odnovolov.forgetmenot.presentation.screen.home.DeckPreviewAdapter.Item.DeckPreview
import com.odnovolov.forgetmenot.presentation.screen.home.HomeEvent.*
import com.soywiz.klock.DateTime
import kotlinx.android.synthetic.main.item_deck_preview.view.*

class DeckPreviewAdapter(
    private val controller: HomeController,
    private val setupHeader: (View) -> Unit
) : ListAdapter<Item, SimpleRecyclerViewHolder>(DiffCallback()) {

    sealed class Item {
        object Header : Item()

        data class DeckPreview(
            val deckId: Long,
            val deckName: String,
            val averageLaps: Double,
            val learnedCount: Int,
            val totalCount: Int,
            val numberOfCardsReadyForExercise: Int?,
            val lastOpened: DateTime?,
            val isSelected: Boolean
        ) : Item()
    }

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TYPE_HEADER else TYPE_ITEM
    }

    /*@ExperimentalStdlibApi*/
    @OptIn(ExperimentalStdlibApi::class)
    fun submitItems(items: List<DeckPreview>) {
        val realItems = buildList {
            add(Item.Header)
            addAll(items)
        }
        super.submitList(realItems)
    }

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
            if (position == 0) return
            val deckPreview: DeckPreview = getItem(position) as DeckPreview
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

    class DiffCallback : DiffUtil.ItemCallback<Item>() {
        override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
            return when {
                oldItem === newItem -> true
                oldItem is DeckPreview && newItem is DeckPreview -> {
                    oldItem.deckId == newItem.deckId
                }
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
            return oldItem == newItem
        }
    }
}