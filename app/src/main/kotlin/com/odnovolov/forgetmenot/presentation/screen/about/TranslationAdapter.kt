package com.odnovolov.forgetmenot.presentation.screen.about

import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.SimpleRecyclerViewHolder
import kotlinx.android.synthetic.main.item_app_translation.view.*

class TranslationAdapter : RecyclerView.Adapter<SimpleRecyclerViewHolder>() {
    override fun getItemCount(): Int = Translation.values().size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleRecyclerViewHolder {
        val itemView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app_translation, parent, false)
        itemView.translatorsTextView.movementMethod = LinkMovementMethod.getInstance()
        return SimpleRecyclerViewHolder(itemView)
    }

    override fun onBindViewHolder(viewHolder: SimpleRecyclerViewHolder, position: Int) {
        val translation: Translation = Translation.values()[position]
        with(viewHolder.itemView) {
            flagTextView.text = translation.flagEmoji
            languageNameTextView.setText(translation.languageNameRes)
            translationProgressTextView.text = translation.progress
            translatorsTextView.text = composeTranslatorsEntry(translation.translators)
        }
    }

    private fun composeTranslatorsEntry(translators: List<Translator>): Spanned {
        val html: String = translators.joinToString { (name: String, link: String?) ->
            if (link != null) {
                """<a href="${link}">${name}</a>"""
            } else {
                name
            }
        }
        return HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY)
    }
}