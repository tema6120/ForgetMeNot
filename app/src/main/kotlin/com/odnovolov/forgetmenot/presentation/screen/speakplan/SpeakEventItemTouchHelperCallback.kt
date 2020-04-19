package com.odnovolov.forgetmenot.presentation.screen.speakplan

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.odnovolov.forgetmenot.presentation.screen.speakplan.SpeakPlanSettingsEvent.SpeakEventItemsMoved
import java.util.*

class SpeakEventItemTouchHelperCallback(
    private val controller: SpeakPlanController,
    private val speakEventAdapter: SpeakEventAdapter
) : ItemTouchHelper.Callback() {
    private val lastPositionFrom = -1
    private val lastPositionTo = -1

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
        val currentPositionFrom = viewHolder.adapterPosition
        val currentPositionTo = target.adapterPosition
        if (currentPositionFrom == lastPositionFrom && currentPositionTo == lastPositionTo) {
            return false
        }
        val newSpeakEvents = speakEventAdapter.currentList
            .map { it.speakEvent }
            .toMutableList()
        Collections.swap(newSpeakEvents, viewHolder.adapterPosition, target.adapterPosition)
        controller.dispatch(SpeakEventItemsMoved(newSpeakEvents))
        return false
    }

    override fun isLongPressDragEnabled() = false

    override fun isItemViewSwipeEnabled() = false

    override fun onSwiped(viewHolder: ViewHolder, direction: Int) {}
}