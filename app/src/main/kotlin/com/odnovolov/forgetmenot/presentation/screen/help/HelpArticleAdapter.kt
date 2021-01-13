package com.odnovolov.forgetmenot.presentation.screen.help

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.SimpleRecyclerViewHolder
import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticle
import kotlinx.android.synthetic.main.item_help_article_help_screen.view.*

class HelpArticleAdapter(
    private val onItemClicked: (HelpArticle) -> Unit
) : RecyclerView.Adapter<SimpleRecyclerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleRecyclerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_help_article_help_screen, parent, false)
        return SimpleRecyclerViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: SimpleRecyclerViewHolder, position: Int) {
        val helpArticle = HelpArticle.values()[position]
        with(viewHolder.itemView) {
            helpArticleIcon.setImageResource(helpArticle.iconRes)
            helpArticleTextView.setText(helpArticle.titleRes)
            helpArticleButton.setOnClickListener { onItemClicked(helpArticle) }
        }
    }

    override fun getItemCount(): Int = HelpArticle.values().size
}