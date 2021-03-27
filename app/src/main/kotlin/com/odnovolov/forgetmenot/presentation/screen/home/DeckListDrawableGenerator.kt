package com.odnovolov.forgetmenot.presentation.screen.home

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.GradientDrawable.Orientation
import android.graphics.drawable.InsetDrawable
import android.graphics.drawable.LayerDrawable
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.dp

object DeckListDrawableGenerator {
    private val generatedIcons: MutableMap<IconColors, Drawable> = HashMap()

    private data class IconColors(
        val strokeColors: List<Int>,
        val backgroundColor: Int
    )

    fun generateIcon(strokeColor: Int): Drawable {
        return generateIcon(listOf(strokeColor), 0)
    }

    fun generateIcon(strokeColors: List<Int>, backgroundColor: Int): Drawable {
        return if (strokeColors.size <= 1) {
            val strokeColor: Int =
                strokeColors.getOrElse(0) { DeckReviewPreference.DEFAULT_DECK_LIST_COLOR }
            generatedIcons.getOrPut(IconColors(strokeColors, backgroundColor)) {
                generateIconInternal(strokeColor)
            }
        } else {
            val firstColor: Int =
                strokeColors.getOrElse(0) { DeckReviewPreference.DEFAULT_DECK_LIST_COLOR }
            val secondColor: Int = strokeColors.getOrElse(1) { firstColor }
            generatedIcons.getOrPut(IconColors(strokeColors, backgroundColor)) {
                generateIconInternal(firstColor, secondColor, backgroundColor)
            }
        }
    }

    private fun generateIconInternal(strokeColor: Int): Drawable {
        return GradientDrawable().mutate {
            cornerRadius = 2.5f.dp
            setStroke(2.5f.dp.toInt(), strokeColor)
            setBounds(0, 0, 10.dp, 10.dp)
        }
    }

    private fun generateIconInternal(
        firstColor: Int,
        secondColor: Int,
        backgroundColor: Int
    ): Drawable {
        val stroke = GradientDrawable().mutate {
            cornerRadius = 3.333f.dp
            colors = intArrayOf(firstColor, secondColor)
            orientation = Orientation.TL_BR
            setBounds(0, 0, 10.dp, 10.dp)
        }
        val backgroundWithoutInset = GradientDrawable().mutate {
            cornerRadius = 1.667f.dp
            setColor(backgroundColor)
            setBounds(0, 0, 5.dp, 5.dp)
        }
        val background = InsetDrawable(backgroundWithoutInset, 2.5f.dp.toInt())
        return LayerDrawable(arrayOf(stroke, background)).mutate {
            setBounds(0, 0, 10.dp, 10.dp)
        }
    }

    private inline fun <reified T : Drawable> T.mutate(action: T.() -> Unit): T {
        (mutate() as T).action()
        return this
    }

    fun generateBackgroundForSelectedItem(color: Int, context: Context): Drawable {
        val startColor = ColorUtils.setAlphaComponent(color, ALPHA_BACKGROUND_START_COLOR)
        val endColor = ContextCompat.getColor(context, R.color.background_selected_deck_list)
        return GradientDrawable().mutate {
            cornerRadius = 8f.dp
            orientation = Orientation.LEFT_RIGHT
            colors = intArrayOf(startColor, endColor, endColor)
        }
    }

    private const val ALPHA_BACKGROUND_START_COLOR = 31
}