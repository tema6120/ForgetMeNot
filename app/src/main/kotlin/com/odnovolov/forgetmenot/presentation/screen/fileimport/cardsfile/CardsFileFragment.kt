package com.odnovolov.forgetmenot.presentation.screen.fileimport.cardsfile

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.Spanned
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import com.brackeys.ui.editorkit.span.ErrorSpan
import com.google.android.material.tabs.TabLayoutMediator
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult
import com.odnovolov.forgetmenot.presentation.common.*
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.screen.fileimport.FileImportController
import com.odnovolov.forgetmenot.presentation.screen.fileimport.FileImportDiScope
import com.odnovolov.forgetmenot.presentation.screen.fileimport.FileImportEvent.*
import com.odnovolov.forgetmenot.presentation.screen.fileimport.cardsfile.CardsFileEvent.*
import com.odnovolov.forgetmenot.presentation.screen.fileimport.cardsfile.CardsFileViewModel.FileImportScreenTitle
import kotlinx.android.synthetic.main.fragment_cards_file.*
import kotlinx.android.synthetic.main.popup_change_deck_for_import.view.*
import kotlinx.android.synthetic.main.popup_charsets.view.*
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class CardsFileFragment : BaseFragment() {
    companion object {
        private const val ARG_ID = "ARG_ID"

        fun create(id: Long) = CardsFileFragment().apply {
            arguments = Bundle(1).apply {
                putLong(ARG_ID, id)
            }
        }
    }

    private var controller: CardsFileController? = null
    private var fileImportController: FileImportController? = null
    private lateinit var viewModel: CardsFileViewModel
    private var changeDeckPopup: PopupWindow? = null
    private var tabLayoutMediator: TabLayoutMediator? = null
    private var sourceTextTab: TextView? = null
    var isAppearingWithAnimation = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cards_file, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = FileImportDiScope.getAsync() ?: return@launch
            controller = diScope.cardsFileController
            fileImportController = diScope.fileImportController
            val id = requireArguments().getLong(ARG_ID)
            fileImportController!!.dispatch(CardsFileIsOpened(id))
            viewModel = diScope.cardsFileViewModel(id)
            observeViewModel()
        }
    }

    private fun setupView() {
        cancelButton.setOnClickListener {
            requireActivity().onBackPressed()
        }
        doneButton.setOnClickListener {
            fileImportController?.dispatch(DoneButtonClicked)
        }
        previousButton.setOnClickListener {
            fileImportController?.dispatch(PreviousButtonClicked)
        }
        nextButton.setOnClickListener {
            fileImportController?.dispatch(NextButtonClicked)
        }
        skipButton.setOnClickListener {
            fileImportController?.dispatch(SkipButtonClicked)
        }
        renameDeckButton.setOnClickListener {
            controller?.dispatch(RenameDeckButtonClicked)
        }
        val delay = if (isAppearingWithAnimation) 450L else 1L
        Handler(Looper.myLooper()!!).postDelayed({
            if (viewCoroutineScope == null) return@postDelayed
            setupViewPager()
            progressBar.isVisible = false
        }, delay)
    }

    private fun setupViewPager() {
        viewPager.offscreenPageLimit = 1
        val id = requireArguments().getLong(ARG_ID)
        viewPager.adapter = CardsFileTabPagerAdapter(id, fragment = this)
        tabLayoutMediator = TabLayoutMediator(
            fileImportTabLayout,
            viewPager
        ) { tab, position ->
            val customTab = View.inflate(requireContext(), R.layout.tab, null) as TextView
            customTab.text = getString(
                when (position) {
                    0 -> R.string.tab_name_cards
                    else -> R.string.tab_name_source_text
                }
            )
            tab.customView = customTab
            if (position == 1) {
                sourceTextTab = customTab
            }
        }.apply { attach() }
    }

    private fun observeViewModel() {
        with(viewModel) {
            screenTitle.observe { screenTitle: FileImportScreenTitle ->
                screenTitleTextView.text = when (screenTitle) {
                    FileImportScreenTitle.Regular -> getString(R.string.screen_title_file_import)
                    is FileImportScreenTitle.Position -> screenTitle.title
                }
            }
            isPreviousButtonEnabled.observe { isPreviousButtonEnabled: Boolean ->
                previousButton.isVisible = isPreviousButtonEnabled
                cancelButton.isVisible = !isPreviousButtonEnabled
            }
            isNextButtonEnabled.observe { isNextButtonEnabled: Boolean ->
                nextButton.isVisible = isNextButtonEnabled
                doneButton.isVisible = !isNextButtonEnabled
            }
            isSkipButtonEnabled.observe { isSkipButtonEnabled: Boolean ->
                skipButton.isVisible = isSkipButtonEnabled
            }
            combine(deckName, deckNameCheckResult) { deckName, deckNameCheckResult ->
                deckNameTextView.text =
                    if (deckNameCheckResult != NameCheckResult.Ok)
                        makeErrorSpannableStringFrom(deckName) else
                        deckName
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
            hasErrorsInSourceText.observe { hasErrors: Boolean ->
                val sourceTextTabTitle = getString(R.string.tab_name_source_text)
                sourceTextTab?.text =
                    if (hasErrors)
                        makeErrorSpannableStringFrom(sourceTextTabTitle) else
                        sourceTextTabTitle
            }
        }
    }

    private fun makeErrorSpannableStringFrom(string: String) =
        SpannableString(string).apply {
            setSpan(
                ErrorSpan(),
                0,
                string.lastIndex,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
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

    override fun onDestroyView() {
        super.onDestroyView()
        tabLayoutMediator?.detach()
        tabLayoutMediator = null
        sourceTextTab = null
        viewPager.adapter = null
        changeDeckPopup?.dismiss()
        changeDeckPopup = null
    }
}