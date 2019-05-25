package com.odnovolov.forgetmenot.presentation.screen

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.feature.deckspreview.DeckPreview
import kotlinx.android.synthetic.main.item_deck_preview.view.*

class DecksPreviewAdapter(
    private val deleteDeckButtonClickCallback: (idx: Int) -> Unit
) : ListAdapter<DeckPreview, DecksPreviewAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_deck_preview, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        getItem(position)?.let { deckPreview: DeckPreview ->
            viewHolder.itemView.deckNameTextView.text = deckPreview.deckName
            viewHolder.itemView.deckOptionButton.setOnClickListener { view: View ->
                showOptionMenu(view, deckPreview.deckId) }
        }
    }

    private fun showOptionMenu(anchor: View, deckId: Int) {
        PopupMenu(anchor.context, anchor).apply {
            inflate(R.menu.deck_preview_actions)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.deleteDeckMenuItem -> {
                        deleteDeckButtonClickCallback.invoke(deckId)
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
        override fun areItemsTheSame(oldDeckPreview: DeckPreview, newDeckPreview: DeckPreview): Boolean {
            return oldDeckPreview.deckId == newDeckPreview.deckId
        }

        override fun areContentsTheSame(oldDeckPreview: DeckPreview, newDeckPreview: DeckPreview): Boolean {
            return oldDeckPreview == newDeckPreview
        }

    }
}