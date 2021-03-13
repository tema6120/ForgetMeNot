package com.odnovolov.forgetmenot.presentation.screen.decklistseditor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.interactor.decklistseditor.EditableDeckList
import kotlinx.coroutines.CoroutineScope

class EditingDeckListAdapter(
    private val coroutineScope: CoroutineScope,
    private val controller: DeckListsEditorController
) : ListAdapter<EditableDeckList, DeckListViewHolder>(DiffCallBack()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeckListViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_editing_deck_list, parent, false)
        return DeckListViewHolder(itemView, coroutineScope, controller)
    }

    override fun onBindViewHolder(viewHolder: DeckListViewHolder, position: Int) {
        val editableDeckList: EditableDeckList = getItem(position)
        viewHolder.bind(editableDeckList)
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).deckList.id
    }

    class DiffCallBack : DiffUtil.ItemCallback<EditableDeckList>() {
        override fun areItemsTheSame(
            oldItem: EditableDeckList,
            newItem: EditableDeckList
        ): Boolean {
            return oldItem.deckList.id == newItem.deckList.id
        }

        override fun areContentsTheSame(
            oldItem: EditableDeckList,
            newItem: EditableDeckList
        ): Boolean = true
    }
}