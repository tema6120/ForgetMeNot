package com.odnovolov.forgetmenot.common.customview

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.PopupWindow
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.common.customview.PresetPopupCreator.PresetRecyclerAdapter.ViewHolder
import com.odnovolov.forgetmenot.presentation.common.dp
import kotlinx.android.synthetic.main.fragment_deck_settings.view.presetNameTextView
import kotlinx.android.synthetic.main.item_preset.view.*

object PresetPopupCreator {
    fun create(
        context: Context,
        setPresetButtonClickListener: (id: Long?) -> Unit,
        renamePresetButtonClickListener: (id: Long) -> Unit,
        deletePresetButtonClickListener: (id: Long) -> Unit,
        addButtonClickListener: () -> Unit,
        takeAdapter: (PresetRecyclerAdapter) -> Unit
    ) = PopupWindow(context).apply {
        width = 256.dp
        height = WindowManager.LayoutParams.WRAP_CONTENT
        setBackgroundDrawable(ColorDrawable(Color.WHITE))
        elevation = 20f
        isOutsideTouchable = true
        isFocusable = true
        val content = View.inflate(context, R.layout.popup_preset, null)
        contentView = content
        val presetRecyclerView = content.findViewById<RecyclerView>(R.id.presetRecyclerView)
        val adapter =
            PresetRecyclerAdapter(
                setPresetButtonClickListener = { id: Long? ->
                    setPresetButtonClickListener.invoke(id)
                    dismiss()
                },
                renamePresetButtonClickListener = renamePresetButtonClickListener,
                deletePresetButtonClickListener = deletePresetButtonClickListener
            )
        presetRecyclerView.adapter = adapter
        val addButton: ImageButton = content.findViewById(R.id.addPresetButton)
        addButton.setOnClickListener {
            addButtonClickListener.invoke()
        }
        takeAdapter(adapter)
    }

    data class Preset(
        val id: Long?,
        val name: String,
        val isSelected: Boolean
    )

    class PresetRecyclerAdapter(
        private val setPresetButtonClickListener: (id: Long?) -> Unit,
        private val renamePresetButtonClickListener: (id: Long) -> Unit,
        private val deletePresetButtonClickListener: (id: Long) -> Unit
    ) : ListAdapter<Preset, ViewHolder>(
        DiffCallback()
    ) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_preset, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            with(viewHolder.itemView) {
                val preset = getItem(position)
                presetNameTextView.text = when {
                    preset.id == null -> context.getString(R.string.off)
                    preset.id == 0L -> context.getString(R.string.default_name)
                    preset.name.isEmpty() -> context.getString(R.string.individual_name)
                    else -> "'${preset.name}'"
                }
                isSelected = preset.isSelected
                setPresetButton.setOnClickListener {
                    setPresetButtonClickListener.invoke(preset.id)
                }
                if (preset.name.isNotEmpty()) {
                    renamePresetButton.visibility = VISIBLE
                    renamePresetButton.setOnClickListener {
                        renamePresetButtonClickListener.invoke(preset.id!!)
                    }
                    deletePresetButton.visibility = VISIBLE
                    deletePresetButton.setOnClickListener {
                        deletePresetButtonClickListener.invoke(preset.id!!)
                    }
                } else {
                    renamePresetButton.visibility = GONE
                    renamePresetButton.setOnClickListener(null)
                    deletePresetButton.visibility = GONE
                    deletePresetButton.setOnClickListener(null)
                }
            }
        }

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

        class DiffCallback : DiffUtil.ItemCallback<Preset>() {
            override fun areItemsTheSame(oldItem: Preset, newItem: Preset): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Preset, newItem: Preset): Boolean =
                oldItem == newItem
        }
    }
}