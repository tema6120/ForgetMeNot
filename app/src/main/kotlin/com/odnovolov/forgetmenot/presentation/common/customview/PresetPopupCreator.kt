package com.odnovolov.forgetmenot.presentation.common.customview

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.PopupWindow
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.dp
import com.odnovolov.forgetmenot.presentation.common.preset.PresetAdapter

object PresetPopupCreator {
    fun create(
        context: Context,
        setPresetButtonClickListener: (id: Long?) -> Unit,
        renamePresetButtonClickListener: (id: Long) -> Unit,
        deletePresetButtonClickListener: (id: Long) -> Unit,
        addButtonClickListener: () -> Unit,
        takeAdapter: (PresetAdapter) -> Unit
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
            PresetAdapter(
                onSetPresetButtonClick = { id: Long? ->
                    setPresetButtonClickListener.invoke(id)
                    dismiss()
                },
                onRenamePresetButtonClick = renamePresetButtonClickListener,
                onDeletePresetButtonClick = deletePresetButtonClickListener
            )
        presetRecyclerView.adapter = adapter
        val addButton: ImageButton = content.findViewById(R.id.addPresetButton)
        addButton.setOnClickListener {
            addButtonClickListener.invoke()
        }
        takeAdapter(adapter)
    }

}