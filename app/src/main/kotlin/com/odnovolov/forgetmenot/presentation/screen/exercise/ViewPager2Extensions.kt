package androidx.viewpager2.widget

import androidx.recyclerview.widget.RecyclerView

fun ViewPager2.findViewHolderForAdapterPosition(position: Int): RecyclerView.ViewHolder? {
    return mRecyclerView.findViewHolderForAdapterPosition(position)
}