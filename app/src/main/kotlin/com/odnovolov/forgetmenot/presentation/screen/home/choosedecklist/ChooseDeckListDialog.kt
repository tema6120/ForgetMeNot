package com.odnovolov.forgetmenot.presentation.screen.home.choosedecklist

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.core.view.isVisible
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseDialogFragment
import com.odnovolov.forgetmenot.presentation.common.createDialog
import com.odnovolov.forgetmenot.presentation.screen.home.ChooseDeckListDialogPurpose.ToAddDeckToDeckList
import com.odnovolov.forgetmenot.presentation.screen.home.ChooseDeckListDialogPurpose.ToRemoveDeckFromDeckList
import com.odnovolov.forgetmenot.presentation.screen.home.HomeController
import com.odnovolov.forgetmenot.presentation.screen.home.HomeDiScope
import com.odnovolov.forgetmenot.presentation.screen.home.HomeEvent.DeckListForAddingDecksSelected
import com.odnovolov.forgetmenot.presentation.screen.home.SelectableDeckListAdapter
import kotlinx.android.synthetic.main.dialog_change_grade.view.*
import kotlinx.android.synthetic.main.dialog_choose_deck_list.view.*
import kotlinx.android.synthetic.main.dialog_title.view.*
import kotlinx.coroutines.launch

class ChooseDeckListDialog : BaseDialogFragment() {
    init {
        HomeDiScope.reopenIfClosed()
    }

    private var controller: HomeController? = null
    private lateinit var viewModel: ChooseDeckListViewModel
    private lateinit var contentView: View
    private lateinit var titleView: View

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog()
        initContentView()
        initTitleView()
        viewCoroutineScope!!.launch {
            val diScope = HomeDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            viewModel = diScope.chooseDeckListViewModel
            observeViewModel()
        }
        return createDialog(contentView, titleView)
    }

    private fun initContentView() {
        contentView = View.inflate(requireContext(), R.layout.dialog_choose_deck_list, null)
    }

    private fun initTitleView() {
        titleView = View.inflate(context, R.layout.dialog_title, null).apply {
            divider.isVisible = contentView.chooseDeckListDialogScrollView.canScrollVertically(-1)
            closeButton.setOnClickListener {
                dismiss()
            }
        }
    }

    private fun observeViewModel() {
        titleView.dialogTitle.setText(R.string.dialog_title_choose_deck_list)
        val adapter = SelectableDeckListAdapter(
            onDeckListButtonClicked = { deckListId: Long? ->
                deckListId ?: return@SelectableDeckListAdapter
                when (viewModel.purpose) {
                    ToAddDeckToDeckList -> {
                        controller?.dispatch(DeckListForAddingDecksSelected(deckListId))
                    }
                    ToRemoveDeckFromDeckList -> TODO()
                }
                dismiss()
            }
        )
        contentView.deckListRecycler.adapter = adapter
        adapter.items = viewModel.selectableDeckLists
        contentView.createDeckListButton.isVisible = viewModel.purpose == ToAddDeckToDeckList
    }

    override fun onResume() {
        super.onResume()
        contentView.chooseDeckListDialogScrollView.viewTreeObserver
            .addOnScrollChangedListener(scrollListener)
    }

    override fun onPause() {
        super.onPause()
        contentView.chooseDeckListDialogScrollView.viewTreeObserver
            .removeOnScrollChangedListener(scrollListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        contentView.deckListRecycler.adapter = null
    }

    private val scrollListener = ViewTreeObserver.OnScrollChangedListener {
        val canScrollUp = contentView.chooseDeckListDialogScrollView.canScrollVertically(-1)
        if (titleView.divider.isVisible != canScrollUp) {
            titleView.divider.isVisible = canScrollUp
        }
    }
}