package com.odnovolov.forgetmenot.presentation.screen.repetition.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.R.layout
import com.odnovolov.forgetmenot.domain.interactor.repetition.RepetitionCard
import com.odnovolov.forgetmenot.presentation.screen.repetition.view.repetitioncard.RepetitionCardController
import com.odnovolov.forgetmenot.presentation.screen.repetition.view.repetitioncard.RepetitionCardViewHolder
import kotlinx.coroutines.CoroutineScope

class RepetitionCardAdapter(
    private val coroutineScope: CoroutineScope,
    private val repetitionCardController: RepetitionCardController
) : RecyclerView.Adapter<RepetitionCardViewHolder>() {
    var items: List<RepetitionCard> = emptyList()
        set(value) {
            if (value != field) {
                field = value
                notifyDataSetChanged()
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepetitionCardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(layout.item_repetition_card, parent, false)
        return RepetitionCardViewHolder(view, coroutineScope, repetitionCardController)
    }

    override fun onBindViewHolder(viewHolder: RepetitionCardViewHolder, position: Int) {
        val repetitionCard: RepetitionCard = items[position]
        viewHolder.bind(repetitionCard)
    }

    override fun getItemCount(): Int = items.size
}