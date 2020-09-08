package com.odnovolov.forgetmenot.presentation.screen.pronunciationplan

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.odnovolov.forgetmenot.presentation.screen.pronunciationplan.PronunciationPlanUiEvent.PronunciationEventItemsMoved

class PronunciationEventItemTouchHelperCallback(
    private val controller: PronunciationPlanController,
    private val adapter: PronunciationEventAdapter
) : ItemTouchHelper.Callback() {
    private val lastFromPosition = -1
    private val lastToPosition = -1

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: ViewHolder): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        val swipeFlags = 0
        return makeMovementFlags(dragFlags, swipeFlags)
    }

    override fun onSelectedChanged(viewHolder: ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            adapter.onStartDragging(viewHolder!!)
        }
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
        controller.dispatch(PronunciationEventItemsMoved(fromPosition, toPosition))
        return true
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        adapter.onStopDragging(viewHolder)
    }

    override fun isLongPressDragEnabled() = false

    override fun isItemViewSwipeEnabled() = false

    override fun onSwiped(viewHolder: ViewHolder, direction: Int) {}
}