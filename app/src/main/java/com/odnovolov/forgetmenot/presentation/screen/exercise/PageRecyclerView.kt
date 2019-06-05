package com.odnovolov.forgetmenot.presentation.screen.exercise

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import java.util.ArrayList

class PageRecyclerView : RecyclerView {

    private val pageSelectingListeners: MutableList<OnPageSelectedListener>
            by lazy { ArrayList<OnPageSelectedListener>() }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        NotifyingPagerSnapHelper(this, pageSelectingListeners).attachToRecyclerView()
    }

    fun addOnPageSelectedListener(listener: OnPageSelectedListener) {
        pageSelectingListeners.add(listener)
    }

    fun removeOnPageSelectedListener(listener: OnPageSelectedListener) {
        pageSelectingListeners.remove(listener)
    }

    interface OnPageSelectedListener {
        fun onPageSelected(index: Int)
    }

    private class NotifyingPagerSnapHelper(
        val recyclerView: RecyclerView,
        val pageSelectingListeners: List<OnPageSelectedListener>
    ) : PagerSnapHelper(), ViewTreeObserver.OnGlobalLayoutListener {

        var lastPage = NO_POSITION

        init {
            recyclerView.viewTreeObserver.addOnGlobalLayoutListener(this)
        }

        fun attachToRecyclerView() {
            super.attachToRecyclerView(recyclerView)
        }

        override fun onGlobalLayout() {
            val position = (recyclerView.layoutManager as LinearLayoutManager)
                .findFirstCompletelyVisibleItemPosition()
            if (position != NO_POSITION) {
                notifyNewPageIfNeeded(position)
                recyclerView.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        }

        override fun findSnapView(layoutManager: LayoutManager?): View? {
            val view = super.findSnapView(layoutManager)
            if (view != null) {
                val selectedPage = recyclerView.getChildAdapterPosition(view)
                notifyNewPageIfNeeded(selectedPage)
            }
            return view
        }

        override fun findTargetSnapPosition(
            layoutManager: LayoutManager?,
            velocityX: Int,
            velocityY: Int
        ): Int {
            val position = super.findTargetSnapPosition(layoutManager, velocityX, velocityY)
            if (recyclerView.adapter != null && position < recyclerView.adapter!!.itemCount) { // Making up for a "bug" in the original snap-helper.
                notifyNewPageIfNeeded(position)
            }
            return position
        }

        fun notifyNewPageIfNeeded(page: Int) {
            if (page != lastPage) {
                pageSelectingListeners.forEach { it.onPageSelected(page) }
                lastPage = page
            }
        }
    }
}