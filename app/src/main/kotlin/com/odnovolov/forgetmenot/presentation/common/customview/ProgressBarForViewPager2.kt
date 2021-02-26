package com.odnovolov.forgetmenot.presentation.common.customview

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.odnovolov.forgetmenot.R

class ProgressBarForViewPager2 @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(
    context,
    attrs,
    defStyleAttr,
    defStyleRes
) {
    private val paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.colorAccent)
    }
    private var animator: ValueAnimator? = null
    private var itemCount = 0
    set(value) {
        if (field != value) {
            field = value
            val level = position / (itemCount - 1f)
            setLevel(level, withAnimation = true)
        }
    }
    private var position = 0
    private var renderedLevel = 0f
    private var detach: (() -> Unit)? = null

    fun setColor(color: Int) {
        paint.color = color
        invalidate()
    }

    fun attach(viewPager2: ViewPager2) {
        val adapter = viewPager2.adapter ?: error("ViewPager2 doesn't have adapter")
        val adapterDataObserver = object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                this@ProgressBarForViewPager2.itemCount = adapter.itemCount
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                this@ProgressBarForViewPager2.itemCount = adapter.itemCount
            }
            override fun onChanged() {
                itemCount = adapter.itemCount
            }
        }
        val onPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
            private var lastPositionOffset = -1f
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                if (lastPositionOffset != positionOffset) {
                    lastPositionOffset = positionOffset
                    this@ProgressBarForViewPager2.position = position
                    val level = (position + positionOffset) / (itemCount - 1)
                    setLevel(level, withAnimation = false)
                }
            }
        }
        adapter.registerAdapterDataObserver(adapterDataObserver)
        viewPager2.registerOnPageChangeCallback(onPageChangeCallback)
        detach = {
            animator?.cancel()
            viewPager2.unregisterOnPageChangeCallback(onPageChangeCallback)
            adapter.unregisterAdapterDataObserver(adapterDataObserver)
        }
    }

    private fun setLevel(level: Float, withAnimation: Boolean) {
        animator?.cancel()
        if (withAnimation) {
            animator = ValueAnimator.ofFloat(this.renderedLevel, level).apply {
                addUpdateListener { animation: ValueAnimator ->
                    this@ProgressBarForViewPager2.renderedLevel = animation.animatedValue as Float
                    invalidate()
                }
                interpolator = DecelerateInterpolator()
                duration = 200
                start()
            }
        } else {
            this.renderedLevel = level
            invalidate()
        }
    }

    fun detach() {
        detach?.invoke()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawRect(0f, 0f, width * renderedLevel, height.toFloat(), paint)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        detach()
    }
}