package com.odnovolov.forgetmenot.common

import android.content.res.Resources
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager.LayoutParams
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.odnovolov.forgetmenot.R
import java.util.*

fun Locale.toFlagEmoji(): String? {
    if (country.length != 2) {
        return null
    }

    val countryCodeCaps = country.toUpperCase() // upper case is important because we are calculating offset
    val firstLetter = Character.codePointAt(countryCodeCaps, 0) - 0x41 + 0x1F1E6
    val secondLetter = Character.codePointAt(countryCodeCaps, 1) - 0x41 + 0x1F1E6

    if (!countryCodeCaps[0].isLetter() || !countryCodeCaps[1].isLetter()) {
        return null
    }

    return String(Character.toChars(firstLetter)) + String(Character.toChars(secondLetter))
}

val Int.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

fun Boolean.toggle(): Boolean = this.not()

fun Fragment.createInputDialog(
    title: CharSequence?,
    takeEditText: (editText: EditText) -> Unit,
    onTextChanged: (text: CharSequence?) -> Unit,
    onPositiveClick: () -> Unit,
    onNegativeClick: () -> Unit
): AlertDialog {
    val contentView = View.inflate(context, R.layout.dialog_input, null)
    val dialogInput: EditText = contentView.findViewById(R.id.dialogInput)
    takeEditText(dialogInput)
    dialogInput.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
            onTextChanged(text)
        }

        override fun afterTextChanged(s: Editable?) {}
    })
    val dialog: AlertDialog = AlertDialog.Builder(requireContext())
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