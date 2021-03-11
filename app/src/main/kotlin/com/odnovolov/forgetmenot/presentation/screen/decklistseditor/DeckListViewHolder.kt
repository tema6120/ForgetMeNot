package com.odnovolov.forgetmenot.presentation.screen.decklistseditor

import android.text.TextWatcher
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.R.string
import com.odnovolov.forgetmenot.domain.entity.DeckList
import com.odnovolov.forgetmenot.presentation.common.observe
import com.odnovolov.forgetmenot.presentation.common.observeText
import com.odnovolov.forgetmenot.presentation.common.showSoftInput
import com.odnovolov.forgetmenot.presentation.screen.decklistseditor.DeckListsEditorEvent.DeckListNameChanged
import com.odnovolov.forgetmenot.presentation.screen.decklistseditor.DeckListsEditorEvent.RemoveDeckListButtonClicked
import com.odnovolov.forgetmenot.presentation.screen.home.DeckListDrawableGenerator
import kotlinx.android.synthetic.main.item_editing_deck_list.view.*
import kotlinx.coroutines.CoroutineScope

class DeckListViewHolder(
    itemView: View,
    private val coroutineScope: CoroutineScope,
    private val controller: DeckListsEditorController
) : RecyclerView.ViewHolder(itemView) {
    private var textWatcher: TextWatcher? = null
    private var viewModel: DeckListViewModel? = null

    init {
        with(itemView) {
            deckListButton.setOnClickListener {
                deckListNameEditText.selectAll()
                deckListNameEditText.showSoftInput()
            }
            deckListNameEditText.setOnFocusChangeListener { v, hasFocus ->
                background = if (hasFocus) {
                    ContextCompat.getDrawable(context, R.drawable.background_editing_deck_list)
                } else {
                    null
                }
                numberOfDecksTextView.isVisible = !hasFocus
                removeDeckListButton.isVisible = hasFocus
                deckListButton.isVisible = !hasFocus
                deckListNameEditText.hint = if (hasFocus)
                    context.getString(string.hint_deck_list_edittext)
                else {
                    null
                }
            }
        }
    }

    fun bind(deckList: DeckList) {
        if (textWatcher != null) {
            itemView.deckListNameEditText.removeTextChangedListener(textWatcher)
        }
        textWatcher = itemView.deckListNameEditText.observeText { newText: String ->
            controller.dispatch(DeckListNameChanged(newText, deckList.id))
        }
        itemView.removeDeckListButton.setOnClickListener {
            controller.dispatch(RemoveDeckListButtonClicked(deckList.id))
        }
        if (viewModel == null) {
            viewModel = DeckListViewModel(deckList)
            observeViewModel()
        } else {
            viewModel!!.setDeckList(deckList)
        }

    }

    private fun observeViewModel() {
        with(viewModel!!) {
            deckListColor.observe(coroutineScope) { deckListColor: Int ->
                val drawable = DeckListDrawableGenerator.generateIcon(listOf(deckListColor), 0)
                itemView.deckListIndicator.setImageDrawable(drawable)
            }
            deckListName.observe(coroutineScope) { deckListName: String ->
                itemView.deckListNameEditText.setText(deckListName)
            }
            deckListSize.observe(coroutineScope) { deckListSize: Int ->
                itemView.numberOfDecksTextView.text = deckListSize.toString()
            }
        }
    }
}