package com.odnovolov.forgetmenot.presentation.screen.decklistseditor

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat.SRC_ATOP
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.*
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.mainactivity.MainActivity
import com.odnovolov.forgetmenot.presentation.screen.decklistseditor.DeckListsEditorController.Command.*
import com.odnovolov.forgetmenot.presentation.screen.decklistseditor.DeckListsEditorEvent.*
import com.odnovolov.forgetmenot.presentation.screen.home.DeckListDrawableGenerator
import com.odnovolov.forgetmenot.presentation.screen.quitwithoutsaving.QuitWithoutSavingBottomSheet
import kotlinx.android.synthetic.main.fragment_deck_lists_editor.*
import kotlinx.android.synthetic.main.item_editing_deck_list.view.*
import kotlinx.android.synthetic.main.popup_deck_list_color_chooser.view.*
import kotlinx.coroutines.launch

class DeckListsEditorFragment : BaseFragment() {
    init {
        DeckListsEditorDiScope.reopenIfClosed()
    }

    private var controller: DeckListsEditorController? = null
    private lateinit var viewModel: DeckListsEditorViewModel
    private var editingDeckListAdapter: EditingDeckListAdapter? = null
    private var newDeckListId: Long? = null
    private var newDeckListColor: Int? = null
    private var colorChooserPopup: PopupWindow? = null
    private val colorAdapter = DeckListColorAdapter(
        onItemClicked = { color: Int ->
            controller?.dispatch(ColorIsSelected(color))
            colorChooserPopup?.dismiss()
        }
    )
    private var lastShownSnackbar: Snackbar? = null

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
            initDeckListAdapter()
            observeViewModel()
            controller!!.commands.observe(::executeCommand)
        }
    }

    private fun setupView() {
        backButton.setOnClickListener {
            controller?.dispatch(BackButtonClicked)
        }
        doneButton.setOnClickListener {
            controller?.dispatch(DoneButtonClicked)
        }
        selectColorForNewDeckListButton.setOnClickListener {
            val newDeckListId = newDeckListId ?: return@setOnClickListener
            controller?.dispatch(SelectDeckListColorButtonClicked(newDeckListId))
        }
        newDeckListNameEditText.setOnFocusChangeListener { v, hasFocus ->
            createNewDeckListFrame.background =
                if (hasFocus) {
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.background_editing_deck_list
                    )
                } else {
                    null
                }
            updateVisibility()
            updateSelectDeckListButtonColor()
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
        expandIcon.isVisible =
            newDeckListNameEditText.hasFocus()
        createDeckListButton.isVisible =
            !newDeckListNameEditText.hasFocus() && newDeckListNameEditText.text.isBlank()
        newDeckListIndicator.isVisible =
            newDeckListNameEditText.hasFocus() || newDeckListNameEditText.text.isNotBlank()
        newDeckListNameEditText.isVisible =
            newDeckListNameEditText.hasFocus() || newDeckListNameEditText.text.isNotBlank()
        saveNewDeckListButton.isVisible =
            newDeckListNameEditText.text.isNotBlank()
    }

    private fun initDeckListAdapter() {
        editingDeckListAdapter = EditingDeckListAdapter(viewCoroutineScope!!, controller!!)
        editingDeckListAdapter!!.setHasStableIds(true)
        deckListsRecyclerView.adapter = editingDeckListAdapter
    }

    private fun observeViewModel() {
        with(viewModel) {
            deckLists.observe(editingDeckListAdapter!!::submitList)
            newDeckListId.observe { newDeckListId: Long ->
                this@DeckListsEditorFragment.newDeckListId = newDeckListId
            }
            newDeckListColor.observe { newDeckListColor: Int ->
                val drawable = DeckListDrawableGenerator.generateIcon(listOf(newDeckListColor), 0)
                newDeckListIndicator.setImageDrawable(drawable)
                this@DeckListsEditorFragment.newDeckListColor = newDeckListColor
                updateSelectDeckListButtonColor()
            }
            if (isForCreation && isViewFirstCreated) {
                newDeckListNameEditText.isVisible = true
                newDeckListNameEditText.post {
                    newDeckListNameEditText.showSoftInput()
                }
            }
        }
    }

    private fun executeCommand(command: DeckListsEditorController.Command) {
        when (command) {
            is ShowColorChooserFor -> {
                val anchor: View = if (command.deckListId == newDeckListId) {
                    selectColorForNewDeckListButton
                } else {
                    val viewHolder =
                        deckListsRecyclerView.findViewHolderForItemId(command.deckListId) ?: return
                    viewHolder.itemView.selectDeckListColorButton
                }
                requireColorChooserPopup().show(anchor, gravity = Gravity.TOP or Gravity.START)
            }
            ShowDeckListIsRemovedMessage -> {
                deckListsEditorRootView.requestFocus()
                hideKeyboardForcibly(requireActivity())
                lastShownSnackbar = Snackbar
                    .make(
                        deckListsEditorRootView,
                        R.string.toast_deck_list_is_removed,
                        resources.getInteger(R.integer.duration_deck_is_deleted_snackbar)
                    )
                    .setAction(
                        R.string.snackbar_action_cancel,
                        { controller?.dispatch(CancelDeckListRemovingButtonClicked) }
                    ).apply {
                        show()
                    }
            }
            is ShowNameCannotBeEmptyMessage -> {
                val viewHolder =
                    deckListsRecyclerView.findViewHolderForItemId(command.deckListId) ?: return
                viewHolder as DeckListViewHolder
                viewHolder.pointAtEmptyName()
            }
            is AskUserToConfirmExit -> {
                QuitWithoutSavingBottomSheet()
                    .show(childFragmentManager, "QuitWithoutSavingBottomSheet")
            }
        }
    }

    private fun requireColorChooserPopup(): PopupWindow {
        if (colorChooserPopup == null) {
            val contentView =
                View.inflate(requireContext(), R.layout.popup_deck_list_color_chooser, null).apply {
                    closeButton.setOnClickListener {
                        colorChooserPopup?.dismiss()
                    }
                    colorRecycler.layoutManager = GridLayoutManager(requireContext(), 8)
                    colorRecycler.adapter = colorAdapter
                    colorEdittext.filters += object : InputFilter {
                        private val hexRegex = Regex("""^\p{XDigit}+${'$'}""")

                        @SuppressLint("DefaultLocale")
                        override fun filter(
                            source: CharSequence,
                            start: Int,
                            end: Int,
                            dest: Spanned,
                            dstart: Int,
                            dend: Int
                        ): CharSequence {
                            return if (source.matches(hexRegex)) {
                                source.toString().toUpperCase()
                            } else {
                                ""
                            }
                        }
                    }
                    colorEdittext.observeText { text: String ->
                        if (text.length == 6) {
                            controller?.dispatch(ColorHexTextIsChanged(text))
                        }
                    }
                    colorEdittext.setOnEditorActionListener { _, actionId, _ ->
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            colorChooserPopup?.dismiss()
                            true
                        } else {
                            false
                        }
                    }
                }
            colorChooserPopup = LightPopupWindow(contentView)
            subscribeColorChooserPopupToViewModel(contentView)
        }
        return colorChooserPopup!!
    }

    private fun subscribeColorChooserPopupToViewModel(contentView: View) {
        viewCoroutineScope!!.launch {
            val diScope = DeckListsEditorDiScope.getAsync() ?: return@launch
            val viewModel = diScope.colorChooserViewModel
            with(viewModel) {
                predefinedColors.observe { predefinedColors: List<SelectableColor> ->
                    colorAdapter.items = predefinedColors
                }
                selectedColor.observe { selectedColor: Int ->
                    contentView.colorEdittext.setDrawableTint(selectedColor)
                }
                hex.observe { hex: String ->
                    if (contentView.colorEdittext.text.toString() != hex) {
                        contentView.colorEdittext.setText(hex)
                    }
                    contentView.colorEdittext.hint = hex
                }
            }
        }
    }

    private fun updateSelectDeckListButtonColor() {
        val deckListColor = newDeckListColor ?: return
        selectColorForNewDeckListButton.background =
            if (newDeckListNameEditText.hasFocus()) {
                val drawable = ContextCompat.getDrawable(
                    requireContext(),
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

    override fun onAttachFragment(childFragment: Fragment) {
        super.onAttachFragment(childFragment)
        when (childFragment) {
            is QuitWithoutSavingBottomSheet -> {
                childFragment.onSaveButtonClicked = {
                    controller?.dispatch(SaveButtonClicked)
                }
                childFragment.onQuitWithoutSavingButtonClicked = {
                    controller?.dispatch(UserConfirmedExit)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        appBar.post { appBar.isActivated = contentScrollView.canScrollVertically(-1) }
        contentScrollView.viewTreeObserver.addOnScrollChangedListener(scrollListener)
        (activity as MainActivity).registerBackPressInterceptor(backPressInterceptor)
    }

    override fun onPause() {
        super.onPause()
        hideKeyboardForcibly(requireActivity())
        contentScrollView.viewTreeObserver.removeOnScrollChangedListener(scrollListener)
        (activity as MainActivity).unregisterBackPressInterceptor(backPressInterceptor)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        colorChooserPopup?.dismiss()
        colorChooserPopup = null
        lastShownSnackbar?.dismiss()
        lastShownSnackbar = null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing()) {
            DeckListsEditorDiScope.close()
        }
    }

    private val backPressInterceptor = MainActivity.BackPressInterceptor {
        controller?.dispatch(BackButtonClicked)
        true
    }

    private val scrollListener = ViewTreeObserver.OnScrollChangedListener {
        val canScrollUp = contentScrollView.canScrollVertically(-1)
        if (appBar.isActivated != canScrollUp) {
            appBar.isActivated = canScrollUp
        }
    }
}