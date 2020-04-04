package com.odnovolov.forgetmenot.presentation.common

import android.content.Context
import android.content.res.Resources
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.odnovolov.forgetmenot.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.runBlocking
import java.util.*

fun Locale.toFlagEmoji(): String? {
    if (country.length != 2) {
        return null
    }

    val countryCodeCaps =
        country.toUpperCase() // upper case is important because we are calculating offset
    val firstLetter = Character.codePointAt(countryCodeCaps, 0) - 0x41 + 0x1F1E6
    val secondLetter = Character.codePointAt(countryCodeCaps, 1) - 0x41 + 0x1F1E6

    if (!countryCodeCaps[0].isLetter() || !countryCodeCaps[1].isLetter()) {
        return null
    }

    return String(Character.toChars(firstLetter)) + String(Character.toChars(secondLetter))
}

val Int.dp: Int get() = (this * Resources.getSystem().displayMetrics.density).toInt()

fun EditText.observeText(onTextChanged: (newText: String) -> Unit) {
    addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
            onTextChanged(text?.toString() ?: "")
        }

        override fun afterTextChanged(s: Editable?) {}
    })
}

fun getBackgroundResForLevelOfKnowledge(levelOfKnowledge: Int) = when (levelOfKnowledge) {
    0 -> R.drawable.background_level_of_knowledge_unsatisfactory
    1 -> R.drawable.background_level_of_knowledge_poor
    2 -> R.drawable.background_level_of_knowledge_acceptable
    3 -> R.drawable.background_level_of_knowledge_satisfactory
    4 -> R.drawable.background_level_of_knowledge_good
    5 -> R.drawable.background_level_of_knowledge_very_good
    else -> R.drawable.background_level_of_knowledge_excellent
}

fun <T> Flow<T>.firstBlocking(): T = runBlocking { first() }

fun <T, R> Flow<T>.mapTwoLatest(block: (old: T, new: T) -> R): Flow<R> {
    class Wrapper(val t: T)

    return scan(Pair<Wrapper?, Wrapper?>(null, null)) { acc: Pair<Wrapper?, Wrapper?>, new: T ->
        acc.second to Wrapper(new)
    }
        .transform { pair: Pair<Wrapper?, Wrapper?> ->
            if (pair.first != null && pair.second != null) {
                emit(block(pair.first!!.t, pair.second!!.t))
            }
        }
}

fun View.showSoftInput(showImplicit: Boolean = false) {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val flags: Int = if (showImplicit) InputMethodManager.SHOW_IMPLICIT else 0
    fun show(): Boolean = requestFocus() && imm.showSoftInput(this, flags)
    if (hasWindowFocus()) {
        val success = show()
        if (!success) post { show() }
    } else {
        viewTreeObserver.addOnWindowFocusChangeListener(
            object : ViewTreeObserver.OnWindowFocusChangeListener {
                override fun onWindowFocusChanged(hasFocus: Boolean) {
                    if (hasFocus) {
                        post { show() }
                        viewTreeObserver.removeOnWindowFocusChangeListener(this)
                    }
                }
            })
    }
}

fun Fragment.showToast(
    stringId: Int,
    duration: Int = Toast.LENGTH_SHORT
) = Toast.makeText(requireContext(), stringId, duration).show()