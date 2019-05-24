package com.odnovolov.forgetmenot.presentation.screen

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.R
import kotlinx.android.synthetic.main.item_deck_preview.view.*

class DecksPreviewAdapter : ListAdapter<String, DecksPreviewAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_deck_preview, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        getItem(position)?.let { deckName: String ->
            viewHolder.itemView.deckNameTextView.text = deckName
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    class DiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldString: String, newString: String): Boolean {
            return oldString == newString
        }

        override fun areContentsTheSame(oldString: String, newString: String): Boolean {
            return oldString == newString
        }

    }
}