package com.odnovolov.forgetmenot.presentation.screen.cardseditor

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.qaeditor.QAEditorFragment

class EditableCardAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    var cardIds: List<Long> = emptyList()
        set(value) {
            if (value != field) {
                field = value
                notifyDataSetChanged()
            }
        }

    override fun createFragment(position: Int): Fragment {
        val id: Long = cardIds[position]
        return QAEditorFragment.create(id)
    }

    override fun getItemCount(): Int = cardIds.size

    override fun getItemId(position: Int): Long = cardIds[position]

    override fun containsItem(itemId: Long): Boolean = cardIds.contains(itemId)
}