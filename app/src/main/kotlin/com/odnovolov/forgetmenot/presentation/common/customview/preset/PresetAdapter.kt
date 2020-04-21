package com.odnovolov.forgetmenot.presentation.common.customview.preset

import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.SimpleRecyclerViewHolder
import kotlinx.android.synthetic.main.item_preset.view.*

class PresetAdapter(
    private val onSetPresetButtonClick: (id: Long?) -> Unit,
    private val onRenamePresetButtonClick: (id: Long) -> Unit,
    private val onDeletePresetButtonClick: (id: Long) -> Unit
) : ListAdapter<Preset, SimpleRecyclerViewHolder>(
    DiffCallback()
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleRecyclerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_preset, parent, false)
        return SimpleRecyclerViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: SimpleRecyclerViewHolder, position: Int) {
        val preset = getItem(position)
        with(viewHolder.itemView) {
            presetNameTextView.text = preset.toString(context)
            setPresetButton.isSelected = preset.isSelected
            setPresetButton.setOnClickListener { onSetPresetButtonClick(preset.id)  }
            if (preset.isShared()) {
                renamePresetButton.visibility = VISIBLE
                renamePresetButton.setOnClickListener { onRenamePresetButtonClick(preset.id!!) }
                deletePresetButton.visibility = VISIBLE
                deletePresetButton.setOnClickListener { onDeletePresetButtonClick(preset.id!!) }
            } else {
                renamePresetButton.visibility = GONE
                renamePresetButton.setOnClickListener(null)
                deletePresetButton.visibility = GONE
                deletePresetButton.setOnClickListener(null)
            }
        }
    }

    class DiffCallback : ItemCallback<Preset>() {
        override fun areItemsTheSame(oldItem: Preset, newItem: Preset) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Preset, newItem: Preset) = oldItem == newItem
    }
}