package com.odnovolov.forgetmenot.presentation.screen.decklistseditor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.entity.DeckList
import kotlinx.coroutines.CoroutineScope

class DeckListAdapter(
    private val coroutineScope: CoroutineScope,
    private val controller: DeckListsEditorController
) : ListAdapter<DeckList, DeckListViewHolder>(DiffCallBack()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeckListViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_editing_deck_list, parent, false)
        return DeckListViewHolder(itemView, coroutineScope, controller)
    }

    override fun onBindViewHolder(viewHolder: DeckListViewHolder, position: Int) {
        val deckList: DeckList = getItem(position)
        viewHolder.bind(deckList)
    }

    class DiffCallBack : DiffUtil.ItemCallback<DeckList>() {
        override fun areItemsTheSame(oldItem: DeckList, newItem: DeckList): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: DeckList, newItem: DeckList): Boolean = true
    }
}