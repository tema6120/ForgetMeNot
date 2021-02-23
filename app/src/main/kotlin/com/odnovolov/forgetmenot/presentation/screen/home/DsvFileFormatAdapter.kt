package com.odnovolov.forgetmenot.presentation.screen.home

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileFormat
import com.odnovolov.forgetmenot.presentation.common.SimpleRecyclerViewHolder

class DsvFileFormatAdapter(
    private val onItemClicked: (FileFormat) -> Unit
) : RecyclerView.Adapter<SimpleRecyclerViewHolder>() {
    var items: List<FileFormat> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleRecyclerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dsv_file_format_for_export, parent, false)
        return SimpleRecyclerViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: SimpleRecyclerViewHolder, position: Int) {
        val fileFormat: FileFormat = items[position]
        val itemView = viewHolder.itemView as TextView
        itemView.text =
            if (fileFormat.isPredefined) {
                fileFormat.name
            } else {
                "'${fileFormat.name}'"
            }
        itemView.setOnClickListener { onItemClicked(fileFormat) }
    }
}