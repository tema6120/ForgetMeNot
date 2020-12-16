package com.odnovolov.forgetmenot.presentation.screen.player.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.R.layout
import com.odnovolov.forgetmenot.domain.interactor.autoplay.PlayingCard
import com.odnovolov.forgetmenot.presentation.screen.player.view.playingcard.PlayingCardController
import com.odnovolov.forgetmenot.presentation.screen.player.view.playingcard.PlayingCardViewHolder
import kotlinx.coroutines.CoroutineScope

class PlayingCardAdapter(
    private val coroutineScope: CoroutineScope,
    private val playingCardController: PlayingCardController
) : RecyclerView.Adapter<PlayingCardViewHolder>() {
    var items: List<PlayingCard> = emptyList()
        set(value) {
            if (value != field) {
                field = value
                notifyDataSetChanged()
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayingCardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(layout.item_playing_card, parent, false)
        return PlayingCardViewHolder(view, coroutineScope, playingCardController)
    }

    override fun onBindViewHolder(viewHolder: PlayingCardViewHolder, position: Int) {
        val playingCard: PlayingCard = items[position]
        viewHolder.bind(playingCard)
    }

    override fun getItemCount(): Int = items.size
}