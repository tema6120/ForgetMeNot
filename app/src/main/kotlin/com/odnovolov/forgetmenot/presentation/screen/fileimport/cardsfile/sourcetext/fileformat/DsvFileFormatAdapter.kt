package com.odnovolov.forgetmenot.presentation.screen.fileimport.cardsfile.sourcetext.fileformat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileFormat
import com.odnovolov.forgetmenot.presentation.common.SimpleRecyclerViewHolder
import kotlinx.android.synthetic.main.item_dsv_file_format.view.*

class DsvFileFormatAdapter(
    private val onItemClicked: (FileFormat) -> Unit,
    private val onViewFormatSettingsButtonClicked: (FileFormat) -> Unit,
    private val onEditFormatSettingsButtonClicked: (FileFormat) -> Unit
) : RecyclerView.Adapter<SimpleRecyclerViewHolder>() {
    var items: List<DsvFileFormat> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleRecyclerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dsv_file_format, parent, false)
        return SimpleRecyclerViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: SimpleRecyclerViewHolder, position: Int) {
        val item = items[position]
        with(viewHolder.itemView) {
            editFormatSettingsButton.isVisible = !item.fileFormat.isPredefined
            viewFormatSettingsButton.isVisible = item.fileFormat.isPredefined
            dsvFormatRadioButton.text = item.fileFormat.name
            dsvFormatRadioButton.isChecked = item.isSelected
            setFormatButton.setOnClickListener {
                onItemClicked(item.fileFormat)
            }
            viewFormatSettingsButton.setOnClickListener {
                onViewFormatSettingsButtonClicked(item.fileFormat)
            }
            editFormatSettingsButton.setOnClickListener {
                onEditFormatSettingsButtonClicked(item.fileFormat)
            }
        }
    }
}

data class DsvFileFormat(
    val fileFormat: FileFormat,
    val isSelected: Boolean
)