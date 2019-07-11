package com.odnovolov.forgetmenot.presentation.screen.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.entity.DeckPreview
import com.odnovolov.forgetmenot.presentation.screen.home.DecksPreviewAdapter.ViewHolder
import com.odnovolov.forgetmenot.presentation.screen.home.HomeScreenFeature.UiEvent
import com.odnovolov.forgetmenot.presentation.screen.home.HomeScreenFeature.UiEvent.DeckButtonClicked
import com.odnovolov.forgetmenot.presentation.screen.home.HomeScreenFeature.UiEvent.DeleteDeckButtonClicked
import com.odnovolov.forgetmenot.presentation.screen.home.HomeScreenFeature.ViewState
import io.reactivex.ObservableSource
import io.reactivex.Observer
import io.reactivex.functions.Consumer
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.item_deck_preview.view.*

class DecksPreviewAdapter
    : ListAdapter<DeckPreview, ViewHolder>(DiffCallback()),
    ObservableSource<UiEvent>,
    Consumer<ViewState> {

    private val uiEventEmitter = PublishSubject.create<UiEvent>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_deck_preview, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        getItem(position)?.let { deckPreview: DeckPreview ->
            viewHolder.itemView.apply {
                setOnClickListener {
                    uiEventEmitter.onNext(DeckButtonClicked(deckPreview.deckId))
                }
                deckNameTextView.text = deckPreview.deckName
                deckOptionButton.setOnClickListener { view: View ->
                    showOptionMenu(view, deckPreview.deckId)
                }
                passedLapsIndicatorTextView.text = deckPreview.passedLaps.toString()
                progressIndicatorTextView.text = deckPreview.progress.toString()
            }
        }
    }

    private fun showOptionMenu(anchor: View, deckId: Int) {
        PopupMenu(anchor.context, anchor).apply {
            inflate(R.menu.deck_preview_actions)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.deleteDeckMenuItem -> {
                        uiEventEmitter.onNext(DeleteDeckButtonClicked(deckId))
                        true
                    }
                    else -> false
                }
            }
            show()
        }
    }

    override fun subscribe(observer: Observer<in UiEvent>) {
        uiEventEmitter.subscribe(observer)
    }

    override fun accept(viewState: ViewState) {
        val displayedDecksPreview = viewState.decksPreview.filter { it.isVisible }
        submitList(displayedDecksPreview)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    class DiffCallback : DiffUtil.ItemCallback<DeckPreview>() {
        override fun areItemsTheSame(oldItem: DeckPreview, newItem: DeckPreview): Boolean {
            return oldItem.deckId == newItem.deckId
        }

        override fun areContentsTheSame(oldItem: DeckPreview, newItem: DeckPreview): Boolean {
            return oldItem == newItem
        }

    }
}