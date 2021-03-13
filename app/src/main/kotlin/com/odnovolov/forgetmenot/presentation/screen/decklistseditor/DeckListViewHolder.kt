package com.odnovolov.forgetmenot.presentation.screen.decklistseditor

import android.text.TextWatcher
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat.SRC_ATOP
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.R.string
import com.odnovolov.forgetmenot.domain.interactor.decklistseditor.EditableDeckList
import com.odnovolov.forgetmenot.presentation.common.observe
import com.odnovolov.forgetmenot.presentation.common.observeText
import com.odnovolov.forgetmenot.presentation.common.showSoftInput
import com.odnovolov.forgetmenot.presentation.screen.decklistseditor.DeckListsEditorEvent.*
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
    private var deckListColor: Int? = null

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
                expandIcon.isVisible = hasFocus
                numberOfDecksTextView.isVisible = !hasFocus
                removeDeckListButton.isVisible = hasFocus
                deckListButton.isVisible = !hasFocus
                deckListNameEditText.hint =
                    if (hasFocus)
                        context.getString(string.hint_deck_list_edittext)
                    else {
                        null
                    }
                updateSelectDeckListButtonColor()
            }
        }
    }

    fun bind(editableDeckList: EditableDeckList) {
        if (textWatcher != null) {
            itemView.deckListNameEditText.removeTextChangedListener(textWatcher)
        }
        textWatcher = itemView.deckListNameEditText.observeText { newText: String ->
            controller.dispatch(DeckListNameChanged(newText, editableDeckList.deckList.id))
        }
        itemView.removeDeckListButton.setOnClickListener {
            controller.dispatch(RemoveDeckListButtonClicked(editableDeckList.deckList.id))
        }
        if (viewModel == null) {
            viewModel = DeckListViewModel(editableDeckList)
            observeViewModel()
        } else {
            viewModel!!.setDeckList(editableDeckList)
        }
    }

    private fun observeViewModel() {
        with(viewModel!!) {
            deckListId.observe(coroutineScope) { deckListId: Long ->
                itemView.selectDeckListColorButton.setOnClickListener {
                    controller.dispatch(SelectDeckListColorButtonClicked(deckListId))
                }
            }
            deckListColor.observe(coroutineScope) { deckListColor: Int ->
                val drawable = DeckListDrawableGenerator.generateIcon(listOf(deckListColor), 0)
                itemView.deckListIndicator.setImageDrawable(drawable)
                this@DeckListViewHolder.deckListColor = deckListColor
                updateSelectDeckListButtonColor()
            }
            deckListName.observe(coroutineScope) { deckListName: String ->
                itemView.deckListNameEditText.setText(deckListName)
            }
            deckListSize.observe(coroutineScope) { deckListSize: Int ->
                itemView.numberOfDecksTextView.text = deckListSize.toString()
            }
        }
    }

    private fun updateSelectDeckListButtonColor() {
        val deckListColor = deckListColor ?: return
        with(itemView) {
            selectDeckListColorButton.background =
                if (deckListNameEditText.hasFocus()) {
                    val drawable = ContextCompat.getDrawable(
                        context,
                        R.drawable.background_select_deck_list_color_button
                    )
                    drawable?.mutate()?.colorFilter =
                        BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                            deckListColor,
                            SRC_ATOP
                        )
                    drawable
                } else {
                    null
                }
        }
    }

    fun pointAtEmptyName() {
        with(itemView.deckListNameEditText) {
            error = context.getString(string.error_message_empty_name)
            showSoftInput()
        }
    }
}