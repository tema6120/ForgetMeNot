package com.odnovolov.forgetmenot.presentation.screen.cardseditor

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.TooltipCompat
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
import kotlinx.android.synthetic.main.popup_intervals.view.*
import kotlinx.coroutines.launch

class CardsEditorFragment : BaseFragment() {
    init {
        CardsEditorDiScope.reopenIfClosed()
    }

    private var controller: CardsEditorController? = null
    private lateinit var viewModel: CardsEditorViewModel
    private val intervalsPopup: PopupWindow by lazy(::createIntervalsPopup)
    private val intervalsAdapter: IntervalsAdapter by lazy(::createIntervalsAdapter)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cards_editor, container, false)
    }

    private fun createIntervalsAdapter(): IntervalsAdapter {
        val onItemClick: (Int) -> Unit = { grade: Int ->
            controller?.dispatch(GradeWasChanged(grade))
            intervalsPopup.dismiss()
        }
        return IntervalsAdapter(onItemClick)
    }

    private fun createIntervalsPopup(): PopupWindow {
        val content: View = View.inflate(context, R.layout.popup_intervals, null).apply {
            intervalsRecycler.adapter = intervalsAdapter
        }
        return PopupWindow(context).apply {
            width = WRAP_CONTENT
            height = WRAP_CONTENT
            contentView = content
            setBackgroundDrawable(
                ContextCompat.getDrawable(requireContext(), R.drawable.background_popup_dark)
            )
            elevation = 20f.dp
            isOutsideTouchable = true
            isFocusable = true
            animationStyle = R.style.PopupFromBottomLeftAnimation
        }
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
            setOnClickListener { controller?.dispatch(GradeButtonClicked) }
            TooltipCompat.setTooltipText(this, contentDescription)
        }
        removeCardButton.run {
            setOnClickListener { controller?.dispatch(RemoveCardButtonClicked) }
            TooltipCompat.setTooltipText(this, contentDescription)
        }
        helpButton.run {
            setOnClickListener { controller?.dispatch(HelpButtonClicked) }
            TooltipCompat.setTooltipText(this, contentDescription)
        }
        cancelButton.run {
            setOnClickListener { controller?.dispatch(CancelButtonClicked) }
            TooltipCompat.setTooltipText(this, contentDescription)
        }
        doneButton.run {
            setOnClickListener { controller?.dispatch(DoneButtonClicked) }
            TooltipCompat.setTooltipText(this, contentDescription)
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
                                    AskAgainButtonClicked else
                                    NotAskButtonClicked
                            )
                        }
                        contentDescription = getString(
                            if (isLearned)
                                R.string.description_mark_as_unlearned_button else
                                R.string.description_mark_as_learned_button
                        )
                        TooltipCompat.setTooltipText(this, contentDescription)
                        isVisible = true
                    }
                }
            }
            isRemoveCardButtonVisible.observe { isVisible: Boolean ->
                removeCardButton.isVisible = isVisible
            }
            helpButton.isVisible = isHelpButtonVisible
        }
    }

    private fun updateGradeButtonColor(grade: Int) {
        val gradeColor: Int = ContextCompat.getColor(requireContext(), getGradeColorRes(grade))
        gradeButton.background.setTint(gradeColor)
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
            is ShowIntervalsPopup -> {
                showIntervalsPopup(command.intervalItems)
            }
            ShowCardIsRemovedMessage -> {
                Snackbar
                    .make(
                        coordinatorLayout,
                        R.string.card_is_removed,
                        resources.getInteger(R.integer.duration_deck_is_deleted_snackbar)
                    )
                    .setAction(
                        R.string.snackbar_action_cancel,
                        { controller?.dispatch(RestoreLastRemovedCardButtonClicked) }
                    )
                    .show()
            }
            AskUserToConfirmExit -> {
                QuitCardsEditorBottomSheet().show(childFragmentManager, "QuitCardsEditorBottomSheet")
            }
        }
    }

    private fun showIntervalsPopup(intervalItems: List<IntervalItem>?) {
        with(intervalsPopup) {
            contentView.intervalsPopupTitleTextView.isActivated = intervalItems != null
            contentView.intervalsRecycler.isVisible = intervalItems != null
            contentView.intervalsAreOffTextView.isVisible = intervalItems == null
            intervalItems?.let { intervalsAdapter.intervalItems = it }
            contentView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
            val gradeButtonLocation = IntArray(2).also(gradeButton::getLocationOnScreen)
            val x = gradeButtonLocation[0] + 8.dp
            val y = gradeButtonLocation[1] + gradeButton.height - 8.dp - contentView.measuredHeight
            showAtLocation(gradeButton.rootView, Gravity.NO_GRAVITY, x, y)
        }
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
    }

    override fun onDestroy() {
        super.onDestroy()
        (activity as AppCompatActivity).supportActionBar?.show()
        if (needToCloseDiScope()) {
            CardsEditorDiScope.close()
        }
    }

    private val onPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            controller?.dispatch(PageSelected(position))
        }
    }

    private val backPressInterceptor = object : MainActivity.BackPressInterceptor {
        override fun onBackPressed(): Boolean {
            controller?.dispatch(BackButtonClicked)
            return true
        }
    }
}