package com.odnovolov.forgetmenot.presentation.screen.help

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.SimpleRecyclerViewHolder
import kotlinx.android.synthetic.main.item_help_article.view.*

class HelpArticleAdapter(
    private val onItemSelected: (HelpArticle) -> Unit
) : ListAdapter<HelpArticleItem, SimpleRecyclerViewHolder>(DiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleRecyclerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_help_article, parent, false)
        return SimpleRecyclerViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: SimpleRecyclerViewHolder, position: Int) {
        val item: HelpArticleItem = getItem(position)
        with(viewHolder.itemView) {
            helpArticleTitleTextView.setText(item.helpArticle.titleId)
            isSelected = item.isArticleSelected
            helpArticleTitleTextView.setOnClickListener {
                onItemSelected(item.helpArticle)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<HelpArticleItem>() {
        override fun areItemsTheSame(oldItem: HelpArticleItem, newItem: HelpArticleItem): Boolean =
            oldItem.helpArticle == newItem.helpArticle

        override fun areContentsTheSame(
            oldItem: HelpArticleItem,
            newItem: HelpArticleItem
        ): Boolean = oldItem == newItem
    }
}