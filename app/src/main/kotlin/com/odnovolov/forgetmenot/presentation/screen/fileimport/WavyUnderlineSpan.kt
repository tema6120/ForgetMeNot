package com.odnovolov.forgetmenot.presentation.screen.fileimport

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.style.LineBackgroundSpan

class WavyUnderlineSpan @JvmOverloads constructor(
    private val color: Int = Color.RED,
    private val lineWidth: Float = 1f,
    private val waveSize: Float = 3f
) : LineBackgroundSpan {

    override fun drawBackground(
        canvas: Canvas,
        paint: Paint,
        left: Int,
        right: Int,
        top: Int,
        baseline: Int,
        bottom: Int,
        text: CharSequence,
        start: Int,
        end: Int,
        lineNumber: Int
    ) {
        val p = Paint(paint)
        p.color = color
        p.strokeWidth = lineWidth
        val width = paint.measureText(text, start, end).toInt()
        val doubleWaveSize = waveSize * 2
        var i = left.toFloat()
        while (i < left + width) {
            canvas.drawLine(i, bottom.toFloat(), i + waveSize, bottom - waveSize, p)
            canvas.drawLine(
                i + waveSize,
                bottom - waveSize,
                i + doubleWaveSize,
                bottom.toFloat(),
                p
            )
            i += doubleWaveSize
        }
    }
}