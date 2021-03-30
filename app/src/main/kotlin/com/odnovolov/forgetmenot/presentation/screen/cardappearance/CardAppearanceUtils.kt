package com.odnovolov.forgetmenot.presentation.screen.cardappearance

import android.content.res.ColorStateList
import android.graphics.Color
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import com.odnovolov.forgetmenot.presentation.common.isDarkMode

val STATES_ENABLED_DISABLED = arrayOf(
    intArrayOf( android.R.attr.state_enabled),
    intArrayOf(-android.R.attr.state_enabled)
)

val STATES_ACTIVATED_DEACTIVATED = arrayOf(
    intArrayOf( android.R.attr.state_activated),
    intArrayOf(-android.R.attr.state_activated)
)

fun TextView.setCardTextColorStateList(
    cardAppearance: CardAppearance,
    states: Array<IntArray> = STATES_ENABLED_DISABLED,
    baseColor: Int = if (context.isDarkMode == true) Color.WHITE else Color.BLACK
) {
    val opacity: Float =
        if (context.isDarkMode == true)
            cardAppearance.textOpacityInDarkTheme else
            cardAppearance.textOpacityInLightTheme
    val alpha: Int = (opacity * 0xFF).toInt()
    val regularTextColor = ColorUtils.setAlphaComponent(baseColor, alpha)
    val learnedTextColor = ColorUtils.setAlphaComponent(baseColor, alpha / 4)
    val colors = intArrayOf(
        regularTextColor,
        learnedTextColor
    )
    val colorStateList = ColorStateList(states, colors)
    setTextColor(colorStateList)
}