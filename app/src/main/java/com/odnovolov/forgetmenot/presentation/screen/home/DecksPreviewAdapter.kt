package com.odnovolov.forgetmenot.presentation.screen.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.feature.deckspreview.DeckPreview
import com.odnovolov.forgetmenot.presentation.screen.home.DecksPreviewAdapter.ViewHolder
import com.odnovolov.forgetmenot.presentation.screen.home.HomeScreen.UiEvent
import com.odnovolov.forgetmenot.presentation.screen.home.HomeScreen.UiEvent.DeckButtonClick
import com.odnovolov.forgetmenot.presentation.screen.home.HomeScreen.UiEvent.DeleteDeckButtonClick
import com.odnovolov.forgetmenot.presentation.screen.home.HomeScreen.ViewState
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
                    uiEventEmitter.onNext(DeckButtonClick(deckPreview.deckId))
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
                        uiEventEmitter.onNext(DeleteDeckButtonClick(deckId))
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
        submitList(viewState.decksPreview)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    class DiffCallback : DiffUtil.ItemCallback<DeckPreview>() {
        override fun areItemsTheSame(oldDeckPreview: DeckPreview, newDeckPreview: DeckPreview): Boolean {
            return oldDeckPreview.deckId == newDeckPreview.deckId
        }

        override fun areContentsTheSame(oldDeckPreview: DeckPreview, newDeckPreview: DeckPreview): Boolean {
            return oldDeckPreview == newDeckPreview
        }

    }
}