package com.odnovolov.forgetmenot.presentation.common

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.os.Handler
import android.text.*
import android.text.Annotation
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.odnovolov.forgetmenot.BuildConfig
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.mainactivity.MainActivity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
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
val Float.dp: Float get() = (this * Resources.getSystem().displayMetrics.density)

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

fun View.hideSoftInput(hideImplicitlyOnly: Boolean = false): Boolean {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val flags: Int = if (hideImplicitlyOnly) InputMethodManager.HIDE_IMPLICIT_ONLY else 0
    return imm.hideSoftInputFromWindow(windowToken, flags)
}

fun Fragment.showToast(
    stringId: Int,
    duration: Int = Toast.LENGTH_SHORT
) = Toast.makeText(requireContext(), stringId, duration).show()

fun Fragment.showToast(
    text: String,
    duration: Int = Toast.LENGTH_SHORT
) = Toast.makeText(requireContext(), text, duration).show()

fun View.uncover() {
    if (visibility != View.VISIBLE) {
        jumpDrawablesToCurrentState()
        visibility = View.VISIBLE
    }
}

inline fun <T> Flow<T>.observe(
    coroutineScope: CoroutineScope,
    crossinline onEach: (value: T) -> Unit
) {
    coroutineScope.launch {
        collect {
            if (isActive) {
                onEach(it)
            }
        }
    }
}

fun TextView.fixTextSelection() {
    setTextIsSelectable(false)
    post { setTextIsSelectable(true) }
}

fun Fragment.needToCloseDiScope(): Boolean {
    return isRemoving || !requireActivity().isChangingConfigurations
}

fun LayoutInflater.inflateAsync(
    layoutResId: Int,
    onInflated: () -> Unit
): FrameLayout {
    val frameLayout = FrameLayout(context)
    GlobalScope.launch(Dispatchers.IO) {
        val view = inflate(layoutResId, frameLayout, false)
        withContext(Dispatchers.Main.immediate) {
            frameLayout.addView(view)
            onInflated()
        }
    }
    return frameLayout
}

inline fun catchAndLogException(block: () -> Unit) {
    try {
        block()
    } catch (e: Exception) {
        if (BuildConfig.DEBUG) {
            Log.d(
                "odnovolov", "Caught Exception that is likely related to" +
                        " asynchronous behavior: ${e.message}"
            )
        }
    }
}

@SuppressLint("RestrictedApi")
fun Fragment.hideActionBar() {
    (activity as MainActivity).supportActionBar?.run {
        setShowHideAnimationEnabled(false)
        hide()
        Handler().post {
            hide()
        }
    }
}

@SuppressLint("RestrictedApi")
fun Fragment.showActionBar() {
    (activity as MainActivity).supportActionBar?.run {
        setShowHideAnimationEnabled(false)
        show()
    }
}

fun TextView.setTextWithClickableAnnotations(
    stringId: Int,
    onAnnotationClick: (annotationValue: String) -> Unit
) {
    val spannedString = context.getText(stringId) as SpannedString
    val spannableString = SpannableString(spannedString)
    spannedString.getSpans(0, spannedString.length, Annotation::class.java)
        .filter { annotation: Annotation -> annotation.key == "clickable" }
        .forEach { annotation: Annotation ->
            val clickableSpan: ClickableSpan = object : ClickableSpan() {
                override fun onClick(textView: View) {
                    onAnnotationClick(annotation.value)
                }

                override fun updateDrawState(textPaint: TextPaint) {
                    super.updateDrawState(textPaint)
                    textPaint.isUnderlineText = true
                }
            }
            spannableString.setSpan(
                clickableSpan,
                spannedString.getSpanStart(annotation),
                spannedString.getSpanEnd(annotation),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    text = spannableString
    movementMethod = LinkMovementMethod.getInstance()
}