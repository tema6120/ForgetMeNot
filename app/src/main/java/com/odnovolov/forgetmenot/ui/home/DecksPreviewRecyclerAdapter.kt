package com.odnovolov.forgetmenot.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.ui.home.DecksPreviewRecyclerAdapter.ViewHolder
import com.odnovolov.forgetmenot.ui.home.HomeViewModel.Event.*
import kotlinx.android.synthetic.main.item_deck_preview.view.*

class DecksPreviewRecyclerAdapter(
    private val viewModel: HomeViewModel
) : ListAdapter<DeckPreview, ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_deck_preview, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        getItem(position)?.let { deckPreview: DeckPreview ->
            viewHolder.itemView.apply {
                setOnClickListener {
                    viewModel.onEvent(DeckButtonClicked(deckPreview.deckId))
                }
                deckNameTextView.text = deckPreview.deckName
                deckOptionButton.setOnClickListener { view: View ->
                    showOptionMenu(view, deckPreview.deckId)
                }
                passedLapsIndicatorTextView.text = deckPreview.passedLaps.toString()
                progressIndicatorTextView.text = deckPreview.progress.toString()
            }
        }
    }

    private fun showOptionMenu(anchor: View, deckId: Int) {
        PopupMenu(anchor.context, anchor).apply {
            inflate(R.menu.deck_preview_actions)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.setupDeckMenuItem -> {
                        viewModel.onEvent(SetupDeckMenuItemClicked(deckId))
                        true
                    }
                    R.id.deleteDeckMenuItem -> {
                        viewModel.onEvent(DeleteDeckMenuItemClicked(deckId))
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