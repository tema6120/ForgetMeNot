package com.odnovolov.forgetmenot.presentation.screen.fileimport

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.SimpleRecyclerViewHolder
import com.odnovolov.forgetmenot.presentation.screen.fileimport.sourcetext.clarifyingName
import kotlinx.android.synthetic.main.item_charset.view.*
import java.nio.charset.Charset

class CharsetAdapter(
    private val onItemClicked: (Charset) -> Unit
) : RecyclerView.Adapter<SimpleRecyclerViewHolder>() {
    var items: List<CharsetItem> = emptyList()
        set(value) {
            if (value != field) {
                field = value
                notifyDataSetChanged()
            }
        }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleRecyclerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_charset, parent, false)
        return SimpleRecyclerViewHolder(view)
    }

    override fun onBindViewHolder(holder: SimpleRecyclerViewHolder, position: Int) {
        val charsetItem: CharsetItem = items[position]
        with(holder.itemView) {
            charsetTextView.text = charsetItem.charset.clarifyingName
            isSelected = charsetItem.isSelected
            setOnClickListener {
                onItemClicked(charsetItem.charset)
            }
        }
    }
}

data class CharsetItem(
    val charset: Charset,
    val isSelected: Boolean
)