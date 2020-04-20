package com.odnovolov.forgetmenot.presentation.screen.speakplan

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.odnovolov.forgetmenot.presentation.screen.speakplan.SpeakPlanUiEvent.SpeakEventItemsMoved

class SpeakEventItemTouchHelperCallback(
    private val controller: SpeakPlanController,
    private val adapter: SpeakEventAdapter
) : ItemTouchHelper.Callback() {
    private val lastFromPosition = -1
    private val lastToPosition = -1

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: ViewHolder): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        val swipeFlags = 0
        return makeMovementFlags(dragFlags, swipeFlags)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: ViewHolder,
        target: ViewHolder
    ): Boolean {
        val fromPosition = viewHolder.adapterPosition
        val toPosition = target.adapterPosition
        if (fromPosition == lastFromPosition && toPosition == lastToPosition) {
            return false
        }
        adapter.onItemMove(fromPosition, toPosition)
        controller.dispatch(SpeakEventItemsMoved(fromPosition, toPosition))
        return true
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        adapter.notifyDraggingStopped()
    }

    override fun isLongPressDragEnabled() = false

    override fun isItemViewSwipeEnabled() = false

    override fun onSwiped(viewHolder: ViewHolder, direction: Int) {}
}