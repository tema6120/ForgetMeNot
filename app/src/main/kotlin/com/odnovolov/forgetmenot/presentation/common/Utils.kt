package com.odnovolov.forgetmenot.presentation.common

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import android.content.res.Resources.NotFoundException
import android.graphics.Color
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.text.*
import android.text.Annotation
import android.text.method.LinkMovementMethod
import android.text.style.BackgroundColorSpan
import android.text.style.ClickableSpan
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.TooltipCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat.Type
import androidx.fragment.app.Fragment
import com.odnovolov.forgetmenot.BuildConfig
import com.odnovolov.forgetmenot.R
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

val Int.sp: Int get() = (this * Resources.getSystem().displayMetrics.scaledDensity).toInt()

fun TextView.setTextSizeFromRes(dimenRes: Int) {
    setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(dimenRes))
}

fun EditText.observeText(onTextChanged: (newText: String) -> Unit) {
    addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
            onTextChanged(text?.toString() ?: "")
        }

        override fun afterTextChanged(s: Editable?) {}
    })
}

fun getBackgroundResForGrade(grade: Int) = when (grade) {
    0 -> R.drawable.background_grade_unsatisfactory
    1 -> R.drawable.background_grade_poor
    2 -> R.drawable.background_grade_acceptable
    3 -> R.drawable.background_grade_satisfactory
    4 -> R.drawable.background_grade_good
    5 -> R.drawable.background_grade_very_good
    else -> R.drawable.background_grade_excellent
}

fun getGradeColorRes(grade: Int) = when (grade) {
    0 -> R.color.grade_unsatisfactory
    1 -> R.color.grade_poor
    2 -> R.color.grade_acceptable
    3 -> R.color.grade_satisfactory
    4 -> R.color.grade_good
    5 -> R.color.grade_very_good
    else -> R.color.grade_excellent
}

fun getBrightGradeColorRes(grade: Int) = when (grade) {
    0 -> R.color.grade_unsatisfactory_bright
    1 -> R.color.grade_poor_bright
    2 -> R.color.grade_acceptable_bright
    3 -> R.color.grade_satisfactory_bright
    4 -> R.color.grade_good_bright
    5 -> R.color.grade_very_good_bright
    else -> R.color.grade_excellent_bright
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

fun View.showSoftInput() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    fun show(): Boolean = requestFocus() && imm.showSoftInput(this, 0)
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

fun View.hideSoftInput(): Boolean {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    return imm.hideSoftInputFromWindow(windowToken, 0)
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
    crossinline onEach: (value: T) -> Unit = {}
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

fun LayoutInflater.inflateAsync(
    layoutResId: Int,
    onInflated: (rootView: FrameLayout, inflatedView: View) -> Unit
): FrameLayout {
    val frameLayout = FrameLayout(context)
    GlobalScope.launch(Dispatchers.IO) {
        val view = inflate(layoutResId, frameLayout, false)
        withContext(Dispatchers.Main.immediate) {
            onInflated(frameLayout, view)
        }
    }
    return frameLayout
}

inline fun doWithCatchingExceptions(block: () -> Unit) {
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

fun String.highlight(
    ranges: List<IntRange>,
    context: Context
): SpannableString {
    val highlightedColor = ContextCompat.getColor(context, R.color.highlighted_text)
    return SpannableString(this).apply {
        ranges.forEach { selection: IntRange ->
            setSpan(
                BackgroundColorSpan(highlightedColor),
                selection.first,
                selection.last,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }
}

fun View.isVisibleOnScreen(): Boolean {
    val actualPosition = Rect().also(::getGlobalVisibleRect)
    val screenWidth = Resources.getSystem().displayMetrics.widthPixels
    val screenHeight = Resources.getSystem().displayMetrics.heightPixels
    val screen = Rect(0, 0, screenWidth, screenHeight)
    return actualPosition.intersect(screen)
}

fun View.getActivity(): Activity? {
    var context = this.context
    while (context is ContextWrapper) {
        if (context is Activity) {
            return context
        }
        context = context.baseContext
    }
    return null
}

fun isKeyboardVisible(view: View): Boolean? =
    ViewCompat.getRootWindowInsets(view)?.isVisible(Type.ime())

fun hideKeyboardForcibly(activity: Activity) {
    val focusedView = activity.currentFocus ?: return
    if (isKeyboardVisible(focusedView) == true) {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(0, 0)
    }
}

fun setStatusBarColor(activity: Activity, colorRes: Int) {
    activity.window.statusBarColor = ContextCompat.getColor(activity, colorRes)
    if (VERSION.SDK_INT >= VERSION_CODES.M) {
        val visibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        activity.window.decorView.systemUiVisibility = visibility
    } else {
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    }
}

fun setTransparentStatusBar(activity: Activity) {
    activity.window.statusBarColor = Color.TRANSPARENT
    if (VERSION.SDK_INT >= VERSION_CODES.M) {
        val visibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        activity.window.decorView.systemUiVisibility = visibility
    } else {
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    }
}

fun getStatusBarHeight(context: Context): Int {
    var result = 0
    val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
    if (resourceId > 0) {
        result = context.resources.getDimensionPixelSize(resourceId)
    }
    return result
}

fun View.setTooltipTextAsContentDescription() {
    TooltipCompat.setTooltipText(this, contentDescription)
}

fun TextView.setFont(fontRes: Int, style: Int) {
    val font: Typeface? = try {
        ResourcesCompat.getFont(context, fontRes)
    } catch (e: NotFoundException) {
        null
    }
    setTypeface(font, style)
}

fun TextView.setFont(fontRes: Int) {
    typeface = try {
        ResourcesCompat.getFont(context, fontRes)
    } catch (e: NotFoundException) {
        return
    }
}