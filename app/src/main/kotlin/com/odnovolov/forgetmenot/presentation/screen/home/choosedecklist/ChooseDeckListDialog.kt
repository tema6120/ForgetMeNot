package com.odnovolov.forgetmenot.presentation.screen.home.choosedecklist

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.core.view.isVisible
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseDialogFragment
import com.odnovolov.forgetmenot.presentation.common.createDialog
import com.odnovolov.forgetmenot.presentation.screen.home.*
import com.odnovolov.forgetmenot.presentation.screen.home.ChooseDeckListDialogPurpose.ToAddDeckToDeckList
import com.odnovolov.forgetmenot.presentation.screen.home.ChooseDeckListDialogPurpose.ToRemoveDeckFromDeckList
import com.odnovolov.forgetmenot.presentation.screen.home.HomeEvent.*
import kotlinx.android.synthetic.main.dialog_change_grade.view.*
import kotlinx.android.synthetic.main.dialog_choose_deck_list.view.*
import kotlinx.android.synthetic.main.dialog_title.view.*
import kotlinx.coroutines.launch

class ChooseDeckListDialog : BaseDialogFragment() {
    init {
        HomeDiScope.reopenIfClosed()
    }

    private var controller: HomeController? = null
    private lateinit var contentView: View
    private lateinit var titleView: View
    private val adapter = SelectableDeckListAdapter(
        onDeckListButtonClicked = { deckListId: Long? ->
            deckListId ?: return@SelectableDeckListAdapter
            val event = when (purpose) {
                ToAddDeckToDeckList -> DeckListForAddingDecksSelected(deckListId)
                ToRemoveDeckFromDeckList -> DeckListForRemovingDecksSelected(deckListId)
                null -> return@SelectableDeckListAdapter
            }
            controller?.dispatch(event)
            dismiss()
        }
    )
    private var purpose: ChooseDeckListDialogPurpose? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog()
        initContentView()
        initTitleView()
        viewCoroutineScope!!.launch {
            val diScope = HomeDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            val viewModel = diScope.chooseDeckListViewModel
            observeViewModel(viewModel)
        }
        return createDialog(contentView, titleView)
    }

    private fun initContentView() {
        contentView = View.inflate(requireContext(), R.layout.dialog_choose_deck_list, null).apply {
            deckListRecycler.adapter = adapter
            createDeckListButton.setOnClickListener {
                controller?.dispatch(CreateDeckListForAddingDecksButtonClicked)
                dismiss()
            }
        }
    }

    private fun initTitleView() {
        titleView = View.inflate(context, R.layout.dialog_title, null).apply {
            dialogTitle.setText(R.string.dialog_title_choose_deck_list)
            closeButton.setOnClickListener {
                dismiss()
            }
            divider.isVisible = contentView.chooseDeckListDialogScrollView.canScrollVertically(-1)
        }
    }

    private fun observeViewModel(viewModel: ChooseDeckListViewModel) {
        with(viewModel) {
            purpose.observe { purpose: ChooseDeckListDialogPurpose? ->
                this@ChooseDeckListDialog.purpose = purpose
                contentView.createDeckListButton.isVisible = purpose == ToAddDeckToDeckList
            }
            selectableDeckLists.observe { selectableDeckLists: List<SelectableDeckList> ->
                adapter.items = selectableDeckLists
            }
        }
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