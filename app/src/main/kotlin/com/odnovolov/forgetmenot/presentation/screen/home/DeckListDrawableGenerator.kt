package com.odnovolov.forgetmenot.presentation.screen.home

import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.GradientDrawable.Orientation
import android.graphics.drawable.InsetDrawable
import android.graphics.drawable.LayerDrawable
import androidx.core.graphics.ColorUtils
import com.odnovolov.forgetmenot.presentation.common.dp

object DeckListDrawableGenerator {
    private val generatedIcons: MutableMap<Pair<Int, Int>, Drawable> = HashMap()

    fun generateIcon(strokeColors: List<Int>, backgroundColor: Int): Drawable {
        return if (strokeColors.size <= 1) {
            val strokeColor: Int =
                strokeColors.getOrElse(0) { DeckReviewPreference.DEFAULT_DECK_LIST_COLOR }
            generatedIcons.getOrPut(strokeColor to strokeColor) {
                generateIcon(strokeColor)
            }
        } else {
            val firstColor: Int =
                strokeColors.getOrElse(0) { DeckReviewPreference.DEFAULT_DECK_LIST_COLOR }
            val secondColor: Int = strokeColors.getOrElse(1) { firstColor }
            generatedIcons.getOrPut(firstColor to secondColor) {
                generateIcon(firstColor, secondColor, backgroundColor)
            }
        }
    }

    private fun generateIcon(strokeColor: Int): Drawable {
        return GradientDrawable().mutate {
            cornerRadius = 2.5f.dp
            setStroke(2.dp, strokeColor)
            setBounds(0, 0, 10.dp, 10.dp)
        }
    }

    private fun generateIcon(firstColor: Int, secondColor: Int, backgroundColor: Int): Drawable {
        val stroke = GradientDrawable().mutate {
            cornerRadius = 3.125f.dp
            colors = intArrayOf(firstColor, secondColor)
            orientation = Orientation.TL_BR
            setBounds(0, 0, 10.dp, 10.dp)
        }
        val backgroundWithoutInset = GradientDrawable().mutate {
            cornerRadius = 1.875f.dp
            setColor(backgroundColor)
            setBounds(0, 0, 6.dp, 6.dp)
        }
        val background = InsetDrawable(backgroundWithoutInset, 2.dp, 2.dp, 2.dp, 2.dp)
        return LayerDrawable(arrayOf(stroke, background)).mutate {
            setBounds(0, 0, 10.dp, 10.dp)
        }
    }

    private inline fun <reified T : Drawable> T.mutate(action: T.() -> Unit): T {
        (mutate() as T).action()
        return this
    }

    fun generateBackgroundForSelectedItem(color: Int): Drawable {
        val startColor = ColorUtils.setAlphaComponent(color, ALPHA_BACKGROUND_START_COLOR)
        return GradientDrawable().mutate {
            cornerRadius = 8f.dp
            orientation = Orientation.LEFT_RIGHT
            colors = intArrayOf(startColor, BACKGROUND_COLOR, BACKGROUND_COLOR)
        }
    }

    private const val ALPHA_BACKGROUND_START_COLOR = 31
    private const val BACKGROUND_COLOR: Int = 0x0F000000
}