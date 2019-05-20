package com.odnovolov.forgetmenot.presentation.screen

import android.system.Os.bind
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.entity.Card
import kotlinx.android.synthetic.main.item_deck_preview.view.*

class DecksPreviewAdapter : ListAdapter<Card, DecksPreviewAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_deck_preview, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        getItem(position)?.let { card: Card ->
            viewHolder.itemView.questionTextView.text = card.question
            viewHolder.itemView.answerTextView.text = card.answer
        }
    }
}

private class DiffCallback : DiffUtil.ItemCallback<Card>() {
    override fun areItemsTheSame(oldCard: Card, newCard: Card): Boolean {
        return oldCard.id == newCard.id
    }

    override fun areContentsTheSame(oldCard: Card, newCard: Card): Boolean {
        return oldCard.equals(newCard)
    }

}