package com.odnovolov.forgetmenot.presentation.common.customview

import android.app.Dialog
import android.content.Context
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.hideSoftInput
import com.odnovolov.forgetmenot.presentation.common.observeText
import com.odnovolov.forgetmenot.presentation.common.showSoftInput

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
        fun hideSoftKeyboard() {
            dialog.currentFocus?.let { focusedView: View -> focusedView.hideSoftInput() }
        }
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                hideSoftKeyboard()
                onPositiveClick()
            }
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener {
                hideSoftKeyboard()
                onNegativeClick()
            }
            dialogInput.showSoftInput()
        }
        return dialog
    }
}