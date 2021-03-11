package com.odnovolov.forgetmenot.presentation.screen.decklistseditor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.hideKeyboardForcibly
import com.odnovolov.forgetmenot.presentation.common.isFinishing
import com.odnovolov.forgetmenot.presentation.common.observeText
import com.odnovolov.forgetmenot.presentation.common.showSoftInput
import com.odnovolov.forgetmenot.presentation.screen.decklistseditor.DeckListsEditorEvent.*
import kotlinx.android.synthetic.main.fragment_deck_lists_editor.*
import kotlinx.coroutines.launch

class DeckListsEditorFragment : BaseFragment() {
    init {
        DeckListsEditorDiScope.reopenIfClosed()
    }

    private var controller: DeckListsEditorController? = null
    private lateinit var viewModel: DeckListsEditorViewModel
    private var adapter: DeckListAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_deck_lists_editor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = DeckListsEditorDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            viewModel = diScope.viewModel
            initAdapter()
            observeViewModel()
        }
    }

    private fun initAdapter() {
        adapter = DeckListAdapter(viewCoroutineScope!!, controller!!)
        deckListsRecyclerView.adapter = adapter
    }

    private fun setupView() {
        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }
        doneButton.setOnClickListener {
            controller?.dispatch(DoneButtonClicked)
        }
        newDeckListNameEditText.setOnFocusChangeListener { v, hasFocus ->
            createNewDeckListFrame.background =
            if (hasFocus) {
                ContextCompat.getDrawable(requireContext(), R.drawable.background_editing_deck_list)
            } else {
                null
            }
            updateVisibility()
        }
        saveNewDeckListButton.setOnClickListener {
            controller?.dispatch(SaveNewDeckListButtonClicked)
            newDeckListNameEditText.text.clear()
        }
        newDeckListNameEditText.observeText { newText: String ->
            controller?.dispatch(NewDeckListNameChanged(newText))
            updateVisibility()
        }
        createDeckListButton.setOnClickListener {
            newDeckListNameEditText.isVisible = true
            newDeckListNameEditText.showSoftInput()
        }
    }

    private fun updateVisibility() {
        createDeckListButton.isVisible =
            !newDeckListNameEditText.hasFocus() && newDeckListNameEditText.text.isEmpty()
        newDeckListIndicator.isVisible =
            newDeckListNameEditText.hasFocus() || newDeckListNameEditText.text.isNotEmpty()
        newDeckListNameEditText.isVisible =
            newDeckListNameEditText.hasFocus() || newDeckListNameEditText.text.isNotEmpty()
        saveNewDeckListButton.isVisible =
            newDeckListNameEditText.text.isNotEmpty()
    }

    private fun observeViewModel() {
        with(viewModel) {
            deckLists.observe(adapter!!::submitList)
        }
    }

    override fun onPause() {
        super.onPause()
        hideKeyboardForcibly(requireActivity())
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing()) {
            DeckListsEditorDiScope.close()
        }
    }
}