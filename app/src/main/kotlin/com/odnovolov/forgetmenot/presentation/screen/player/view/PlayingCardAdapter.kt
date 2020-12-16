package com.odnovolov.forgetmenot.presentation.screen.player.view

import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.interactor.autoplay.PlayingCard
import com.odnovolov.forgetmenot.presentation.common.customview.AsyncFrameLayout
import com.odnovolov.forgetmenot.presentation.screen.exercise.KnowingWhenPagerStopped
import com.odnovolov.forgetmenot.presentation.screen.player.view.playingcard.PlayingCardController
import com.odnovolov.forgetmenot.presentation.screen.player.view.playingcard.PlayingCardViewHolder
import kotlinx.coroutines.CoroutineScope

class PlayingCardAdapter(
    private val coroutineScope: CoroutineScope,
    private val knowingWhenPagerStopped: KnowingWhenPagerStopped,
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
        val layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
        val asyncFrameLayout = AsyncFrameLayout(layoutParams, parent.context)
        asyncFrameLayout.inflateAsync(R.layout.item_playing_card)
        return PlayingCardViewHolder(
            asyncFrameLayout,
            coroutineScope,
            playingCardController,
            knowingWhenPagerStopped
        )
    }

    override fun onBindViewHolder(viewHolder: PlayingCardViewHolder, position: Int) {
        val playingCard: PlayingCard = items[position]
        viewHolder.bind(playingCard)
    }

    override fun getItemCount(): Int = items.size
}