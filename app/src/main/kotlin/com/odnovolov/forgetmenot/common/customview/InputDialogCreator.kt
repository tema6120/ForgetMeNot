package com.odnovolov.forgetmenot.common.customview

import android.app.Dialog
import android.content.Context
import android.view.View
import android.view.WindowManager.LayoutParams
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.common.observeText

object InputDialogCreator {
    fun create(
        context: Context,
        title: CharSequence?,
        takeEditText: (editText: EditText) -> Unit,
        onTextChanged: (text: String) -> Unit,
        onPositiveClick: () -> Unit,
        onNegativeClick: () -> Unit
    ): Dialog {
        val contentView = View.inflate(context, R.layout.dialog_input, null)
        val dialogInput: EditText = contentView.findViewById(R.id.dialogInput)
        takeEditText(dialogInput)
        dialogInput.observeText {
            onTextChanged(it.toString())
        }
        val dialog: AlertDialog = AlertDialog.Builder(context)
            .setTitle(title)
            .setView(contentView)
            .setCancelable(false)
            .setPositiveButton(android.R.string.ok, null)
            .setNegativeButton(android.R.string.cancel, null)
            .create()
        dialog.window?.setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener { onPositiveClick() }
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener { onNegativeClick() }
            dialogInput.requestFocus()
        }
        return dialog
    }
}