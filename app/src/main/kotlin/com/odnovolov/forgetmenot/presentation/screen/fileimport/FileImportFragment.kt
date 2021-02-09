package com.odnovolov.forgetmenot.presentation.screen.fileimport

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.view.GravityCompat
import com.brackeys.ui.editorkit.span.ErrorSpan
import com.google.android.material.tabs.TabLayoutMediator
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult
import com.odnovolov.forgetmenot.presentation.common.*
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.screen.fileimport.FileImportEvent.*
import kotlinx.android.synthetic.main.fragment_file_import.*
import kotlinx.android.synthetic.main.popup_change_deck_for_import.view.*
import kotlinx.android.synthetic.main.popup_charsets.view.*
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class FileImportFragment : BaseFragment() {
    init {
        FileImportDiScope.reopenIfClosed()
    }

    private var controller: FileImportController? = null
    private lateinit var viewModel: FileImportViewModel
    private var changeDeckPopup: PopupWindow? = null
    private var tabLayoutMediator: TabLayoutMediator? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_file_import, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = FileImportDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            viewModel = diScope.viewModel
            observeViewModel()
        }
    }

    private fun setupView() {
        previousButton.setOnClickListener {
            controller?.dispatch(CancelButtonClicked)
        }
        nextButton.setOnClickListener {
            controller?.dispatch(DoneButtonClicked)
        }
        renameDeckButton.setOnClickListener {
            controller?.dispatch(RenameDeckButtonClicked)
        }
        setupViewPager()
    }

    private fun setupViewPager() {
        fileImportViewPager.offscreenPageLimit = 1
        fileImportViewPager.adapter = FileImportPagerAdapter(this)
        tabLayoutMediator = TabLayoutMediator(
            fileImportTabLayout,
            fileImportViewPager
        ) { tab, position ->
            val customTab = View.inflate(requireContext(), R.layout.tab, null) as TextView
            customTab.text = getString(
                when (position) {
                    0 -> R.string.tab_name_source_text
                    1 -> R.string.tab_name_cards
                    else -> throw IllegalArgumentException("position must be in 0..1")
                }
            )
            tab.customView = customTab
        }.apply { attach() }
    }

    private fun observeViewModel() {
        with(viewModel) {
            combine(deckName, deckNameCheckResult) { deckName, deckNameCheckResult ->
                deckNameTextView.text = if (deckNameCheckResult == NameCheckResult.Occupied) {
                    SpannableString(deckName).apply {
                        setSpan(
                            ErrorSpan(),
                            0,
                            deckName.lastIndex,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                } else {
                    deckName
                }
                deckNameTextView.error = when (deckNameCheckResult) {
                    NameCheckResult.Ok -> null
                    NameCheckResult.Empty -> getString(R.string.error_message_empty_name)
                    NameCheckResult.Occupied -> getString(R.string.error_message_occupied_name)
                }
                if (deckNameCheckResult != NameCheckResult.Ok) {
                    deckNameTextView.requestFocus()
                }
            }.observe()
            isNewDeck.observe { isNewDeck: Boolean ->
                deckLabelTextView.setText(
                    if (isNewDeck)
                        R.string.deck_label_in_file_import_new else
                        R.string.deck_label_in_file_import_existing
                )
                deckLabelTextView.setBackgroundResource(
                    if (isNewDeck)
                        R.drawable.background_new_deck else
                        R.drawable.deck_label_in_file_import_existing
                )
                changeDeckButton.setOnClickListener {
                    if (isNewDeck) {
                        controller?.dispatch(AddCardsToExistingDeckButtonClicked)
                    } else {
                        showChangeDeckPopup()
                    }
                }
            }
        }
    }

    private fun showChangeDeckPopup() {
        requireChangeDeckPopup().show(
            anchor = changeDeckButton,
            gravity = Gravity.TOP or GravityCompat.END
        )
    }

    private fun requireChangeDeckPopup(): PopupWindow {
        if (changeDeckPopup == null) {
            val content: View = View.inflate(
                requireContext(),
                R.layout.popup_change_deck_for_import,
                null
            ).apply {
                newDeckButton.setOnClickListener {
                    changeDeckPopup!!.dismiss()
                    controller?.dispatch(AddCardsToNewDeckButtonClicked)
                }
                existingDeckButton.setOnClickListener {
                    changeDeckPopup!!.dismiss()
                    controller?.dispatch(AddCardsToExistingDeckButtonClicked)
                }
            }
            changeDeckPopup = LightPopupWindow(content)
        }
        return changeDeckPopup!!
    }

    // called from ImportedTextEditorFragment if there are no errors at the start
    fun goToCardsTab() {
        fileImportViewPager?.run {
            post { setCurrentItem(1, false) }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        changeDeckPopup?.dismiss()
        changeDeckPopup = null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (needToCloseDiScope()) {
            FileImportDiScope.close()
        }
    }
}