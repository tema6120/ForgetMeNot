package com.odnovolov.forgetmenot.presentation.screen.cardseditor

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.snackbar.Snackbar
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.*
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.mainactivity.MainActivity
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.CardsEditorController.Command.*
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.CardsEditorEvent.*
import com.odnovolov.forgetmenot.presentation.screen.exercise.IntervalItem
import com.odnovolov.forgetmenot.presentation.screen.exercise.IntervalsAdapter
import kotlinx.android.synthetic.main.fragment_cards_editor.*
import kotlinx.android.synthetic.main.popup_card_info.view.*
import kotlinx.android.synthetic.main.popup_intervals.view.*
import kotlinx.coroutines.launch

class CardsEditorFragment : BaseFragment() {
    init {
        CardsEditorDiScope.reopenIfClosed()
    }

    private var controller: CardsEditorController? = null
    private lateinit var viewModel: CardsEditorViewModel
    private var intervalsPopup: PopupWindow? = null
    private var intervalsAdapter: IntervalsAdapter? = null
    private var cardInfoPopup: PopupWindow? = null
    private var lastShownSnackbar: Snackbar? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setTransparentStatusBar(requireActivity())
        return inflater.inflate(R.layout.fragment_cards_editor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = CardsEditorDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            viewModel = diScope.viewModel
            observeViewModel()
            controller!!.commands.observe(::executeCommand)
        }
    }

    private fun setupView() {
        cardsViewPager.adapter = EditableCardAdapter(this)
        cardsViewPager.registerOnPageChangeCallback(onPageChangeCallback)
        gradeButton.run {
            setOnClickListener { showIntervalsPopup() }
            setTooltipTextFromContentDescription()
        }
        removeCardButton.run {
            setOnClickListener { controller?.dispatch(RemoveCardButtonClicked) }
            setTooltipTextFromContentDescription()
        }
        moveCardButton.run {
            setOnClickListener { controller?.dispatch(MoveCardButtonClicked) }
            setTooltipTextFromContentDescription()
        }
        copyCardButton.run {
            setOnClickListener { controller?.dispatch(CopyCardButtonClicked) }
            setTooltipTextFromContentDescription()
        }
        cardInfoButton.run {
            setOnClickListener { controller?.dispatch(CardInfoButtonClicked) }
            setTooltipTextFromContentDescription()
        }
        helpButton.run {
            setOnClickListener { controller?.dispatch(HelpButtonClicked) }
            setTooltipTextFromContentDescription()
        }
        cancelButton.run {
            setOnClickListener { controller?.dispatch(CancelButtonClicked) }
            setTooltipTextFromContentDescription()
        }
        doneButton.run {
            setOnClickListener { controller?.dispatch(DoneButtonClicked) }
            setTooltipTextFromContentDescription()
        }
    }

    private fun observeViewModel() {
        with(viewModel) {
            val editableCardAdapter = cardsViewPager.adapter as EditableCardAdapter
            cardIds.observe { cardIds: List<Long> ->
                editableCardAdapter.cardIds = cardIds
                if (cardsViewPager.currentItem != currentPosition) {
                    cardsViewPager.setCurrentItem(currentPosition, false)
                }
            }
            hasCards.observe { hasCards: Boolean ->
                noCardsTextView.isVisible = !hasCards
            }
            gradeOfCurrentCard.observe { grade: Int? ->
                gradeButton.isVisible = grade != null
                if (grade != null) {
                    gradeButton.text = grade.toString()
                    updateGradeButtonColor(grade)
                }
            }
            isCurrentEditableCardLearned.observe { isLearned: Boolean? ->
                with(markAsLearnedButton) {
                    if (isLearned == null) {
                        isVisible = false
                    } else {
                        setImageResource(
                            if (isLearned)
                                R.drawable.ic_mark_as_unlearned else
                                R.drawable.ic_mark_as_learned
                        )
                        setOnClickListener {
                            controller?.dispatch(
                                if (isLearned)
                                    MarkAsUnlearnedButtonClicked else
                                    MarkAsLearnedButtonClicked
                            )
                        }
                        contentDescription = getString(
                            if (isLearned)
                                R.string.description_mark_as_unlearned_button else
                                R.string.description_mark_as_learned_button
                        )
                        setTooltipTextFromContentDescription()
                        isVisible = true
                    }
                }
            }
            isCurrentCardMovable.observe { isCurrentCardMovable: Boolean ->
                moveCardButton.isVisible = isCurrentCardMovable
                copyCardButton.isVisible = isCurrentCardMovable
            }
            isCurrentCardRemovable.observe { isCurrentCardRemovable: Boolean ->
                removeCardButton.isVisible = isCurrentCardRemovable
            }
            cardInfoButton.isVisible = isCardInfoButtonVisible
            helpButton.isVisible = isHelpButtonVisible
        }
    }

    private fun updateGradeButtonColor(grade: Int) {
        val gradeColorRes: Int = getGradeColorRes(grade)
        gradeButton.setBackgroundTintFromRes(gradeColorRes)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            val brightGradeColor: Int =
                ContextCompat.getColor(requireContext(), getBrightGradeColorRes(grade))
            gradeButton.outlineAmbientShadowColor = brightGradeColor
            gradeButton.outlineSpotShadowColor = brightGradeColor
        }
    }

    private fun executeCommand(command: CardsEditorController.Command) {
        when (command) {
            is ShowUnfilledTextInputAt -> {
                cardsViewPager.setCurrentItem(command.position, true)
                showToast(R.string.toast_fill_in_the_card)
            }
            ShowCardIsRemovedMessage -> {
                lastShownSnackbar = Snackbar
                    .make(
                        coordinatorLayout,
                        R.string.card_is_removed,
                        resources.getInteger(R.integer.duration_deck_is_deleted_snackbar)
                    )
                    .setAction(
                        R.string.snackbar_action_cancel,
                        { controller?.dispatch(RestoreLastRemovedCardButtonClicked) }
                    ).apply {
                        show()
                    }
            }
            ShowCardIsMovedMessage -> {
                lastShownSnackbar = Snackbar
                    .make(
                        coordinatorLayout,
                        R.string.card_is_moved,
                        resources.getInteger(R.integer.duration_deck_is_deleted_snackbar)
                    )
                    .setAction(
                        R.string.snackbar_action_cancel,
                        { controller?.dispatch(CancelLastMovementButtonClicked) }
                    ).apply {
                        show()
                    }
            }
            ShowCardIsCopiedMessage -> {
                lastShownSnackbar = Snackbar
                    .make(
                        coordinatorLayout,
                        R.string.card_is_copied,
                        resources.getInteger(R.integer.duration_deck_is_deleted_snackbar)
                    )
                    .setAction(
                        R.string.snackbar_action_cancel,
                        { controller?.dispatch(CancelLastCopyingButtonClicked) }
                    ).apply {
                        show()
                    }
            }
            is ShowCardInfo -> {
                showCardInfoPopup(command.cardInfo)
            }
            AskUserToConfirmExit -> {
                QuitCardsEditorBottomSheet().show(
                    childFragmentManager,
                    "QuitCardsEditorBottomSheet"
                )
            }
        }
    }

    private fun showIntervalsPopup() {
        requireIntervalsPopup().show(anchor = gradeButton, gravity = Gravity.BOTTOM)
    }

    private fun requireIntervalsPopup(): PopupWindow {
        if (intervalsPopup == null) {
            val content: View = View.inflate(context, R.layout.popup_intervals, null)
            val onItemClick: (Int) -> Unit = { grade: Int ->
                intervalsPopup?.dismiss()
                controller?.dispatch(GradeWasSelected(grade))
            }
            intervalsAdapter = IntervalsAdapter(onItemClick)
            content.intervalsRecycler.adapter = intervalsAdapter
            intervalsPopup = DarkPopupWindow(content)
            subscribeIntervalsPopupToViewModel()
        }
        return intervalsPopup!!
    }

    private fun subscribeIntervalsPopupToViewModel() {
        viewCoroutineScope!!.launch {
            val diScope = CardsEditorDiScope.getAsync() ?: return@launch
            diScope.viewModel.intervalItems.observe { intervalItems: List<IntervalItem>? ->
                intervalsPopup?.contentView?.run {
                    intervalItems?.let { intervalsAdapter!!.intervalItems = it }
                    intervalsIcon.isActivated = intervalItems != null
                    intervalsRecycler.isVisible = intervalItems != null
                    intervalsAreOffTextView.isVisible = intervalItems == null
                }
            }
        }
    }

    private fun showCardInfoPopup(cardInfo: CardInfo) {
        val cardInfoPopup: PopupWindow = requireCardInfoPopup()
        cardInfoPopup.contentView.apply {
            deckNameTextView.text = cardInfo.deckName
            numberOfTestsTextView.text = cardInfo.numberOfTests
            timeOfLastTestTextView.text = cardInfo.timeOfLastTest
        }
        cardInfoPopup.show(anchor = cardInfoButton, gravity = Gravity.BOTTOM)
    }

    private fun requireCardInfoPopup(): PopupWindow {
        if (cardInfoPopup == null) {
            val content = View.inflate(requireContext(), R.layout.popup_card_info, null)
            cardInfoPopup = DarkPopupWindow(content)
        }
        return cardInfoPopup!!
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.run {
            val needToShowIntervalsPopup = getBoolean(STATE_INTERVALS_POPUP, false)
            if (needToShowIntervalsPopup) {
                showIntervalsPopup()
            }
            val needToShowCardInfoPopup = getBoolean(STATE_CARD_INFO_POPUP, false)
            if (needToShowCardInfoPopup) {
                controller?.dispatch(CardInfoButtonClicked)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val isIntervalsPopupShowing = intervalsPopup?.isShowing ?: false
        outState.putBoolean(STATE_INTERVALS_POPUP, isIntervalsPopupShowing)
        val isCardInfoPopupShowing = cardInfoPopup?.isShowing ?: false
        outState.putBoolean(STATE_CARD_INFO_POPUP, isCardInfoPopupShowing)
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).registerBackPressInterceptor(backPressInterceptor)
    }

    override fun onPause() {
        super.onPause()
        (activity as MainActivity).unregisterBackPressInterceptor(backPressInterceptor)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cardsViewPager.unregisterOnPageChangeCallback(onPageChangeCallback)
        cardsViewPager.adapter = null
        intervalsPopup?.dismiss()
        intervalsPopup = null
        intervalsAdapter = null
        cardInfoPopup?.dismiss()
        cardInfoPopup = null
        lastShownSnackbar?.dismiss()
        lastShownSnackbar = null
    }

    override fun onDestroy() {
        super.onDestroy()
        (activity as AppCompatActivity).supportActionBar?.show()
        if (isFinishing()) {
            CardsEditorDiScope.close()
        }
    }

    private val onPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            controller?.dispatch(PageWasChanged(position))
        }
    }

    private val backPressInterceptor = MainActivity.BackPressInterceptor {
        controller?.dispatch(BackButtonClicked)
        true
    }

    companion object {
        private const val STATE_INTERVALS_POPUP = "STATE_INTERVALS_POPUP"
        private const val STATE_CARD_INFO_POPUP = "STATE_CARD_INFO_POPUP"
    }
}