package com.odnovolov.forgetmenot.presentation.common

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Point
import android.graphics.Rect
import android.transition.Fade
import android.transition.Transition
import android.transition.TransitionValues
import android.view.Gravity
import android.view.View
import android.view.View.OnLayoutChangeListener
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.ViewTreeObserver
import android.view.animation.DecelerateInterpolator
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import com.odnovolov.forgetmenot.R

fun LightPopupWindow(content: View): PopupWindow =
    createPopupWindow(content, R.drawable.background_popup_light)

fun DarkPopupWindow(content: View): PopupWindow =
    createPopupWindow(content, R.drawable.background_popup_dark)

private fun createPopupWindow(content: View, backgroundRes: Int) =
    PopupWindow(content).apply {
        width = WRAP_CONTENT
        height = WRAP_CONTENT
        contentView = content
        setBackgroundDrawable(ContextCompat.getDrawable(content.context, backgroundRes))
        elevation = 20f.dp
        isOutsideTouchable = true
        isFocusable = true
    }

fun PopupWindow.show(anchor: View) {
    PopupPositioner(this, anchor).show()
}

private class PopupPositioner(
    private val popupWindow: PopupWindow,
    private val anchor: View
) : OnLayoutChangeListener {
    private val contentWidth: Int get() = popupWindow.contentView.width
    private val contentHeight: Int get() = popupWindow.contentView.height
    private val popupPosition = Point()

    fun show() {
        with(popupWindow) {
            contentView.doOnNextGlobalLayout {
                position()
                observeLayoutChanges()
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                enterTransition = PopUp()
                exitTransition = Fade()
            } else {
                animationStyle = R.style.Animation_Popup_Before_23_API
            }
            showAtLocation(anchor.rootView, Gravity.NO_GRAVITY, 0, 0)
        }
    }

    private fun observeLayoutChanges() {
        popupWindow.contentView.addOnLayoutChangeListener(this)
        anchor.addOnLayoutChangeListener(this)
        popupWindow.setOnDismissListener {
            popupWindow.contentView.removeOnLayoutChangeListener(this)
            anchor.removeOnLayoutChangeListener(this)
        }
    }

    private fun position() {
        val window = Rect().also(anchor::getWindowVisibleDisplayFrame)
        val (anchorX, anchorY) = IntArray(2).also(anchor::getLocationOnScreen)

        val bestX = anchorX + anchor.width / 2 - contentWidth / 2
        val x = when {
            contentWidth >= window.width() -> {
                0
            }
            contentWidth >= window.width() - POPUP_MARGIN * 2 -> {
                window.width() / 2 - contentWidth / 2
            }
            bestX < POPUP_MARGIN -> {
                POPUP_MARGIN
            }
            bestX + contentWidth > window.right - POPUP_MARGIN -> {
                window.right - POPUP_MARGIN - contentWidth
            }
            else -> {
                bestX
            }
        }

        val y = when {
            contentHeight >= window.height() -> {
                0
            }
            contentHeight >= window.height() - POPUP_MARGIN * 2 -> {
                window.height() / 2 - contentHeight / 2
            }
            anchorY < POPUP_MARGIN -> {
                POPUP_MARGIN
            }
            anchorY + contentHeight > window.bottom - POPUP_MARGIN -> {
                window.bottom - POPUP_MARGIN - contentHeight
            }
            else -> {
                anchorY
            }
        }

        if (popupPosition.x != x || popupPosition.y != y) {
            popupPosition.x = x
            popupPosition.y = y
            popupWindow.update(x, y, popupWindow.width, popupWindow.height)

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                popupWindow.contentView?.parent?.run {
                    if (this !is View) return@run
                    pivotX = anchorX + anchor.width / 2f - x
                    pivotY = anchorY + anchor.height / 2f - y
                }
            }
        }
    }

    override fun onLayoutChange(
        v: View?,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        oldLeft: Int,
        oldTop: Int,
        oldRight: Int,
        oldBottom: Int
    ) {
        position()
    }

    private inline fun View.doOnNextGlobalLayout(crossinline action: () -> Unit) {
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                action()
            }
        })
    }

    companion object {
        private val POPUP_MARGIN: Int = 8.dp
    }
}

private class PopUp : Transition() {
    // we don't use captured values but it's important to put different values to trigger
    // createAnimator() method
    override fun captureStartValues(transitionValues: TransitionValues) {
        transitionValues.values[PROPNAME_SCALE_X] = 0f
    }

    override fun captureEndValues(transitionValues: TransitionValues) {
        transitionValues.values[PROPNAME_SCALE_X] = 1f
    }

    override fun createAnimator(
        sceneRoot: ViewGroup?,
        startValues: TransitionValues?,
        endValues: TransitionValues?
    ): Animator? {
        if (startValues == null || endValues == null) return null
        val view = startValues.view
        return AnimatorSet().apply {
            duration = 200
            interpolator = DecelerateInterpolator()
            playTogether(
                ObjectAnimator.ofFloat(view, View.SCALE_X, 0f, 1f),
                ObjectAnimator.ofFloat(view, View.SCALE_Y, 0f, 1f),
                ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f)
            )
        }
    }

    companion object {
        private const val PROPNAME_SCALE_X = "forgetmenot:scale:scaleX"
    }
}